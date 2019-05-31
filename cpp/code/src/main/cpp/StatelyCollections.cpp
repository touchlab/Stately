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

    class ListIterBox {
    public:
        ListIterBox(const StatelyList::iterator &iterator, StatelyList *list) : iterator_(iterator), list_(list) {
        }

        StatelyList::iterator iterator_;
        StatelyList *list_;
        bool beforeStart = false;
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

    void Stately_list_clear(KLong ptr) {
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
      return pBox->iterator_ != pBox->list_->begin();
    }

    KBoolean Stately_list_iterHasNext(KLong ptr, KRef nativeMemory) {
      Locker locker(&(((FastList *) ptr)->lock_));
      ListIterBox *pBox = castIter(nativeMemory);
      return pBox->iterator_ != pBox->list_->end();
    }

    OBJ_GETTER(Stately_list_iterNext, KLong ptr, KRef nativeMemory) {
      Locker locker(&(((FastList *) ptr)->lock_));
      ListIterBox *pBox = castIter(nativeMemory);
      if(pBox->iterator_ == pBox->list_->end())
      {
        Throw_NoSuchElementException();
      }
      std::list<void *, KonanAllocator<void *>>::iterator &iterator = pBox->iterator_;
      auto retval = *iterator;
      pBox->iterator_++;

      RETURN_OBJ((KRef) retval);
    }

    OBJ_GETTER(Stately_list_iterPrevious, KLong ptr, KRef nativeMemory) {
      Locker locker(&(((FastList *) ptr)->lock_));
      ListIterBox *pBox = castIter(nativeMemory);
      if(pBox->iterator_ == pBox->list_->begin())
      {
        Throw_NoSuchElementException();
      }
      std::list<void *, KonanAllocator<void *>>::iterator &iterator = pBox->iterator_;
      auto retval = *iterator;
      pBox->iterator_--;

      RETURN_OBJ((KRef) retval);
    }



    void Stately_list_iterAdd(KLong ptr, KRef nativeMemory, KRef value) {
      Locker locker(&(((FastList *) ptr)->lock_));
      ListIterBox *pBox = (ListIterBox *) nativeMemToPointer(nativeMemory);
//      pBox->rewind();
      pBox->iterator_ = pBox->list_->insert(pBox->iterator_, CreateStablePointer(value));
    }

    void Stately_list_iterRemove(KLong ptr, KRef nativeMemory) {
      Locker locker(&(((FastList *) ptr)->lock_));
      ListIterBox *pBox = castIter(nativeMemory);
//      pBox->rewind();
      void *&entry = *(pBox->iterator_);
      pBox->iterator_ = pBox->list_->erase(pBox->iterator_);
      DisposeStablePointer(entry);
    }

    void Stately_list_iterSet(KLong ptr, KRef nativeMemory, KRef value) {
      Locker locker(&(((FastList *) ptr)->lock_));
      ListIterBox *pBox = castIter(nativeMemory);
//      pBox->rewind();
      void *&entry = *(pBox->iterator_);
      *(pBox->iterator_) = CreateStablePointer(value);
      DisposeStablePointer(entry);
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

            if(traceCount < 50)
            {
              cppTrace(((KRef )it->first)->container()->refCount());
              cppTrace(((KRef )currentValue)->container()->refCount());
            }

            DisposeStablePointer(it->first);

            if(traceCount++ < 50)
            {
              cppTrace(((KRef )it->first)->container()->refCount());
              cppTrace(((KRef )currentValue)->container()->refCount());
            }

            map_.erase(it);
          }

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

        void setTempVar(KRef a){
          tempvar_ = CreateStablePointer(a);
        }
        
        KRef grabTempVar(){
          DisposeStablePointer(tempvar_);
          KRef resultVal = (KRef) tempvar_;
          tempvar_ = nullptr;
          return resultVal;
        }

        KInt tempRefCount(){
          KRef resultVal = (KRef) tempvar_;
          return resultVal->container()->refCount();
        }

    private:
        KInt traceCount = 0;
        StatelyMap map_;
        pthread_mutexattr_t attr_;
        pthread_mutex_t lock_;
        KNativePtr tempvar_;
    };

    extern "C" {
    KLong Stately_map_create() {
      FastHashMap *map = konanConstructInstance<FastHashMap>();
      return (KLong) map;
    }

    void Stately_map_clear(KLong mapPtr) {
      ((FastHashMap *) mapPtr)->clear();
      konanDestructInstance((FastHashMap *) mapPtr);
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

    void Stately_map_setTempVar(KLong mapPtr, KRef a) {
      ((FastHashMap *) mapPtr)->setTempVar(a);
    }

    KRef Stately_map_grabTempVar(KLong mapPtr) {
      return ((FastHashMap *) mapPtr)->grabTempVar();
    }

    KInt Stately_debug_refCount(KRef a){
      return a->container()->refCount();
    }

    KInt Stately_map_tempRefCount(KLong mapPtr) {
      return ((FastHashMap *) mapPtr)->tempRefCount();
    }


    }
} // namespace stately
