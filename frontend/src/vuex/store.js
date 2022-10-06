import Vue from "vue";
import Vuex from "vuex";

Vue.use(Vuex);
// Create a new store instance.
export const store = new Vuex.Store({
  state() {
    return {
      pipelineList: [],
      tempPipelineList: [],
      pipelineVo: {
      },
      userInfo: {
        userId: "",
        name: "",
        nickName: "",
        phone: "",
      },
      pipelineInfo: [

      ],
      tableShowMode: "", // REGISTER , UPDATE
      tableUpdateFlag: false
    };
  },
  mutations: {
    increment(state) {
      state.count++;
    },
  },
});
