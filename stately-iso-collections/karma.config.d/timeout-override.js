// Override test timeout. Needed for stress tests
config.set({
  "client": {
    "mocha": {
      "timeout": 60000
    },
  },
});