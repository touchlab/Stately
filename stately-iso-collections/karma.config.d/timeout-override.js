// Override test timeout. Needed for stress tests
config.set({
  "client": {
    "mocha": {
      "timeout": 120000
    },
  },
  "browserDisconnectTimeout": 120000
});