import Vue from "vue";
import Vuex from "vuex";

Vue.use(Vuex);
// Create a new store instance.
export const store = new Vuex.Store({
  state() {
    return {
      pipelineVo: {
        id: "",
        creator: "",
        name: "",
        detail: "",
        status: "",
        dataSet: "",
        processorGroupId: null,
        collector: {},
        filter: {},
        converter: {},
      },
    };
  },
  mutations: {
    increment(state) {
      state.count++;
    },
  },
});
