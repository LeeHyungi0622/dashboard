import Vue from "vue";
import Vuex from "vuex";

Vue.use(Vuex);
// Create a new store instance.
export const store = new Vuex.Store({
  state() {
    return {
      pipelineList: [],
      pipelineVo: {},
    };
  },
  mutations: {
    increment(state) {
      state.count++;
    },
  },
});
