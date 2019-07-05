/*
 * Copyright (C) 2019 Touchlab, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <stdlib.h>
#include <list>
#include <pthread.h>
#include "Types.h"
#include "Alloc.h"
#include "Natives.h"
#include "Memory.h"
#include "KAssert.h"

extern "C" {
KInt generalHash(KRef a);
KBoolean generalEquals(KRef a, KRef b);
OBJ_GETTER(makeByteMemory, KInt size);
void cppTrace(KInt traceVal);
RUNTIME_NORETURN void Throw_IndexOutOfBoundsException();
RUNTIME_NORETURN void Throw_NoSuchElementException();
}

namespace stately {

    class Locker {
    public:
        explicit Locker(pthread_mutex_t *lock) : lock_(lock) {
          pthread_mutex_lock(lock_);
        }

        ~Locker() {
          pthread_mutex_unlock(lock_);
        }

    private:
        pthread_mutex_t *lock_;
    };

    struct KeyBoxHash {
        std::size_t operator()(KNativePtr keyVal) const {
          size_t keyHash = generalHash((KRef) keyVal);
          return (keyHash);
        }
    };

    struct KeyBoxEq {
        bool operator()(KNativePtr a, KNativePtr b) const {
          return generalEquals((KRef) a, (KRef) b);
        }
    };

    using StatelyMap = std::unordered_map<void *, void *, stately::KeyBoxHash, stately::KeyBoxEq, KonanAllocator<std::__1::pair<void *const, void *>>>;
    using StatelyList = std::list<void *, KonanAllocator<void *>>;

    void *nativeMemToPointer(KRef nativeMemory) {
      return (void *) ArrayAddressOfElementAt(nativeMemory->array(), 0);
    }

    class ListNodeBox {
    public:
        ListNodeBox(const StatelyList::iterator &iterator, StatelyList *list) : iterator_(iterator), list_(list) {
        }

        void readd() {
          void **pVoid = &*iterator_;
          void* entry = *pVoid;
          list_->erase(iterator_);
          list_->push_back(entry);
          auto endIter = list_->end();
          endIter--;
          iterator_ = endIter;
        }

        void remove() {
          void *&entry = *iterator_;
          list_->erase(iterator_);
          DisposeStablePointer(entry);
        }

    private:
        StatelyList::iterator iterator_;
        StatelyList *list_;
    };

    class ListIterBox {
    public:
        ListIterBox(const StatelyList::iterator &iterator, StatelyList *list) : iterator_(iterator), list_(list) {
        }

        KBoolean hasPrevious() {
          return !freshIter;
        }

        KBoolean hasNext() {
          const std::list<void *, KonanAllocator<void *>>::iterator &iterEnd = list_->end();

          if(list_->size() == 0 || iterator_ == iterEnd)
            return false;

          if(freshIter)
            return true;

          iterator_++;
          bool nextEnd = iterator_ == iterEnd;
          iterator_--;
          return !nextEnd;
        }

        OBJ_GETTER0(next) {
          const std::list<void *, KonanAllocator<void *>>::iterator &iterEnd = list_->end();

          if(iterator_ == iterEnd)
            Throw_NoSuchElementException();

          if(freshIter)
            freshIter = false;
          else
            iterator_++;

          if(iterator_ == iterEnd)
            Throw_NoSuchElementException();

          auto retval = *iterator_;

          RETURN_OBJ((KRef) retval);
        }

        OBJ_GETTER0(previous) {
          _rewind();

          auto retval = *iterator_;

          RETURN_OBJ((KRef) retval);
        }

        void _rewind(){
          if(freshIter)
          {
            Throw_NoSuchElementException();
          }

          if(iterator_ == list_->begin())
            freshIter = true;
          else
            iterator_--;
        }

        void add(KRef value) {
          if(freshIter)
          {
            Throw_NoSuchElementException();
          }

          RuntimeAssert(PermanentOrFrozen(value), "Must be frozen - iter add");

          iterator_ = list_->insert(iterator_, CreateStablePointer(value));
        }

        void remove() {
          if(freshIter)
          {
            Throw_NoSuchElementException();
          }

          void **pVoid = &*iterator_;
          void * entry = *pVoid;
          DisposeStablePointer(entry);

          iterator_ = list_->erase(iterator_);

          _rewind();


        }

        void set(KRef value) {
          void **pVoid = &*iterator_;
          DisposeStablePointer(*pVoid);
          RuntimeAssert(PermanentOrFrozen(value), "Must be frozen - iter set");
          *iterator_ = CreateStablePointer(value);
        }

    private:
        StatelyList::iterator iterator_;
        StatelyList *list_;
        bool freshIter = true;
    };

    class FastList {
    public:
        FastList() {
          pthread_mutexattr_init(&attr_);
          pthread_mutexattr_settype(&attr_, PTHREAD_MUTEX_RECURSIVE);
          pthread_mutex_init(&lock_, &attr_);
        }

        ~FastList() {
          pthread_mutex_destroy(&lock_);
        }

        KInt size() {
          Locker locker(&lock_);
          return list_.size();
        }

        void add(KRef value) {
          Locker locker(&lock_);
          RuntimeAssert(PermanentOrFrozen(value), "Must be frozen - list add");
          list_.push_back(CreateStablePointer(value));
        }

        void clear() {
          Locker locker(&lock_);
          auto it = list_.begin();
          while (it != list_.end()){
            DisposeStablePointer(*it);
            it++;
          }
          list_.clear();
        }

        OBJ_GETTER0(beginIter) {
          Locker locker(&lock_);
          auto it = list_.begin();

          KRef nativeMemory = makeByteMemory(sizeof(ListIterBox), OBJ_RESULT);
          auto iterBox = new(nativeMemToPointer(nativeMemory)) ListIterBox(list_.begin(), &list_);

          RETURN_OBJ(nativeMemory);
        }

        OBJ_GETTER(addNode, KRef value) {
          Locker locker(&lock_);
          RuntimeAssert(PermanentOrFrozen(value), "Must be frozen - list addNode");
          list_.push_back(CreateStablePointer(value));

          KRef nativeMemory = makeByteMemory(sizeof(ListNodeBox), OBJ_RESULT);
          auto endIter = list_.end();
          endIter--;
          auto iterBox = new(nativeMemToPointer(nativeMemory)) ListNodeBox(endIter, &list_);

          RETURN_OBJ(nativeMemory);
        }

        void lock(){
          pthread_mutex_lock(&lock_);
        }

        void unlock(){
          pthread_mutex_unlock(&lock_);
        }

        pthread_mutex_t lock_;
    private:
        StatelyList list_;

        pthread_mutexattr_t attr_;
    };

    extern "C" {
    KLong Stately_list_create() {
      FastList *map = konanConstructInstance<FastList>();
      return (KLong) map;
    }

    KInt Stately_lest_size(KLong ptr) {
      return ((FastList *) ptr)->size();
    }

    void Stately_list_add(KLong ptr, KRef value) {
      ((FastList *) ptr)->add(value);
    }

    OBJ_GETTER(Stately_list_addNode, KLong ptr, KRef value) {
      FastList *pList = (FastList *) ptr;

      RETURN_RESULT_OF(pList->addNode, value);
    }

    ListNodeBox * castNode(KRef nativeMemory){
      return (ListNodeBox *) nativeMemToPointer(nativeMemory);
    }

    void Stately_list_nodeRemove(KLong ptr, KRef nativeMemory) {
      Locker locker(&(((FastList *) ptr)->lock_));
      ListNodeBox *pBox = castNode(nativeMemory);
      pBox->remove();
    }

    void Stately_list_nodeReadd(KLong ptr, KRef nativeMemory) {
      Locker locker(&(((FastList *) ptr)->lock_));
      ListNodeBox *pBox = castNode(nativeMemory);
      pBox->readd();
    }

    void Stately_list_clear(KLong ptr) {
      Locker locker(&(((FastList *) ptr)->lock_));
      ((FastList *) ptr)->clear();
    }

    OBJ_GETTER(Stately_list_beginIter, KLong ptr) {
      FastList *pList = (FastList *) ptr;
      RETURN_RESULT_OF0(pList->beginIter);
    }

    ListIterBox * castIter(KRef nativeMemory){
      return (ListIterBox *) nativeMemToPointer(nativeMemory);
    }

    KBoolean Stately_list_iterHasPrevious(KLong ptr, KRef nativeMemory) {
      Locker locker(&(((FastList *) ptr)->lock_));
      ListIterBox *pBox = castIter(nativeMemory);
      return pBox->hasPrevious();
    }

    KBoolean Stately_list_iterHasNext(KLong ptr, KRef nativeMemory) {
      Locker locker(&(((FastList *) ptr)->lock_));
      ListIterBox *pBox = castIter(nativeMemory);
      return pBox->hasNext();
    }

    OBJ_GETTER(Stately_list_iterNext, KLong ptr, KRef nativeMemory) {
      Locker locker(&(((FastList *) ptr)->lock_));
      ListIterBox *pBox = castIter(nativeMemory);
      RETURN_RESULT_OF0(pBox->next);
    }

    OBJ_GETTER(Stately_list_iterPrevious, KLong ptr, KRef nativeMemory) {
      Locker locker(&(((FastList *) ptr)->lock_));
      ListIterBox *pBox = castIter(nativeMemory);
      RETURN_RESULT_OF0(pBox->previous);
    }

    void Stately_list_iterAdd(KLong ptr, KRef nativeMemory, KRef value) {
      Locker locker(&(((FastList *) ptr)->lock_));
      ListIterBox *pBox = (ListIterBox *) nativeMemToPointer(nativeMemory);
      pBox->add(value);
    }

    void Stately_list_iterRemove(KLong ptr, KRef nativeMemory) {
      Locker locker(&(((FastList *) ptr)->lock_));
      ListIterBox *pBox = castIter(nativeMemory);
      pBox->remove();
    }

    void Stately_list_iterSet(KLong ptr, KRef nativeMemory, KRef value) {
      Locker locker(&(((FastList *) ptr)->lock_));
      ListIterBox *pBox = castIter(nativeMemory);
      pBox->set(value);
    }

    void Stately_list_lock(KLong ptr) {
      ((FastList *) ptr)->lock();
    }

    void Stately_list_unlock(KLong ptr) {
      ((FastList *) ptr)->unlock();
    }
    }

    class IterBox {
    public:
        IterBox(const StatelyMap::iterator &iterator, StatelyMap *map) : iterator_(iterator), map_(map) {
        }

        StatelyMap::iterator iterator_;
        StatelyMap *map_;
    };

    class FastHashMap {
    public:
        FastHashMap() {
          pthread_mutexattr_init(&attr_);
          pthread_mutexattr_settype(&attr_, PTHREAD_MUTEX_RECURSIVE);
          pthread_mutex_init(&lock_, &attr_);
        }

        ~FastHashMap() {
          pthread_mutex_destroy(&lock_);
        }

        KInt size() {
          Locker locker(&lock_);
          return map_.size();
        }

        void clear() {
          Locker locker(&lock_);
          auto it = map_.begin();
          while (it != map_.end()) {
            DisposeStablePointer(it->first);
            DisposeStablePointer(it->second);
            it++;
          }
          map_.clear();
        }

        KBoolean contains(KRef key) {
          Locker locker(&lock_);
          return map_.find((KNativePtr) key) != map_.end();
        }

        KBoolean containsValue(KRef value) {
          Locker locker(&lock_);
          auto it = map_.begin();
          while (it != map_.end()) {
            if (generalEquals(value, (KRef) it->second))
              return true;
            it++;
          }

          return false;
        }

        OBJ_GETTER(put, KRef key, KRef value) {
          Locker locker(&lock_);
          auto it = map_.find(key);
          KNativePtr currentValue = nullptr;
          if (it != map_.end()) {
            currentValue = it->second;

            DisposeStablePointer(it->first);

            map_.erase(it);
          }

          RuntimeAssert(PermanentOrFrozen(key), "Must be frozen - map put key");
          RuntimeAssert(PermanentOrFrozen(value), "Must be frozen - map put value");

          auto keyPtr = CreateStablePointer(key);
          auto valuePtr = CreateStablePointer(value);

          map_[keyPtr] = valuePtr;

          if(currentValue != nullptr) {
            auto result = AdoptStablePointer(currentValue, OBJ_RESULT);
            return result;
          } else{
            RETURN_OBJ(nullptr);
          }
        }

        OBJ_GETTER(remove, KRef key) {
          Locker locker(&lock_);
          auto it = map_.find(key);
          KNativePtr currentValue = nullptr;
          if (it != map_.end()) {
            currentValue = it->second;
            DisposeStablePointer(it->first);
            map_.erase(it);
          }

          if(currentValue != nullptr) {
            auto result = AdoptStablePointer(currentValue, OBJ_RESULT);
            return result;
          } else{
            RETURN_OBJ(nullptr);
          }
        }

        OBJ_GETTER(get, KRef key) {
          Locker locker(&lock_);
          auto it = map_.find(key);
          KNativePtr currentValue = nullptr;
          if (it == map_.end()) {
            RETURN_OBJ(nullptr);
          } else {
            RETURN_OBJ((KRef)it->second);
          }
        }

        KRef beginIter() {
          Locker locker(&lock_);
          auto it = map_.begin();

          ObjHolder kotlinValueHolder;

          KRef nativeMemory = makeByteMemory(sizeof(IterBox), kotlinValueHolder.slot());
          auto iterBox = new(nativeMemToPointer(nativeMemory)) IterBox(map_.begin(), &map_);

          return nativeMemory;
        }

    private:
        StatelyMap map_;
        pthread_mutexattr_t attr_;
        pthread_mutex_t lock_;
    };

    extern "C" {
    KLong Stately_map_create() {
      FastHashMap *map = konanConstructInstance<FastHashMap>();
      return (KLong) map;
    }

    void Stately_map_clear(KLong mapPtr) {
      ((FastHashMap *) mapPtr)->clear();
    }

    void Stately_map_destroy(KLong mapPtr) {
      ((FastHashMap *) mapPtr)->clear();
      konanDestructInstance((FastHashMap *) mapPtr);
    }

    KInt Stately_map_size(KLong mapPtr) {
      return ((FastHashMap *) mapPtr)->size();
    }

    KBoolean Stately_map_contains(KLong mapPtr, KRef key) {
      return ((FastHashMap *) mapPtr)->contains(key);
    }

    KBoolean Stately_map_containsValue(KLong mapPtr, KRef value) {
      return ((FastHashMap *) mapPtr)->containsValue(value);
    }

    OBJ_GETTER(Stately_map_put, KLong mapPtr, KRef key, KRef value) {
      return ((FastHashMap *) mapPtr)->put(key, value, OBJ_RESULT);
    }

    OBJ_GETTER(Stately_map_remove, KLong mapPtr, KRef key) {
      return ((FastHashMap *) mapPtr)->remove(key, OBJ_RESULT);
    }

    OBJ_GETTER(Stately_map_get, KLong mapPtr, KRef key) {
      RETURN_OBJ(((FastHashMap *) mapPtr)->get(key, OBJ_RESULT));
    }

    OBJ_GETTER(Stately_map_beginIter, KLong mapPtr) {
      RETURN_OBJ(((FastHashMap *) mapPtr)->beginIter());
    }

    KBoolean Stately_map_iterHasNext(KRef nativeMemory) {
      IterBox *pBox = (IterBox *) nativeMemToPointer(nativeMemory);
      return pBox->iterator_ != pBox->map_->end();
    }

    OBJ_GETTER(Stately_map_iterNextKey, KRef nativeMemory) {
      IterBox *pBox = (IterBox *) nativeMemToPointer(nativeMemory);
      auto retval = pBox->iterator_->first;
      pBox->iterator_++;

      RETURN_OBJ((KRef) retval);
    }

    OBJ_GETTER(Stately_map_iterNextValue, KRef nativeMemory) {
      IterBox *pBox = (IterBox *) nativeMemToPointer(nativeMemory);
      auto retval = pBox->iterator_->second;
      pBox->iterator_++;

      RETURN_OBJ((KRef) retval);
    }

    void Stately_map_iterRemove(KRef nativeMemory) {
      IterBox *pBox = (IterBox *) nativeMemToPointer(nativeMemory);
      pBox->iterator_ = pBox->map_->erase(pBox->iterator_);
    }

    /**
     * Get size of this object. Used to init byte array.
     * @return
     */
    KInt Stately_map_iterBoxSize() {
      return sizeof(IterBox);
    }

    KInt Stately_debug_refCount(KRef a){
      return a->container()->refCount();
    }

    }
} // namespace stately
