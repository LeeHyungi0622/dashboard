import Vue from "vue";
import Vuex from "vuex";

Vue.use(Vuex);
// Create a new store instance.
export const store = new Vuex.Store({
  state() {
    return {
      commandList: [],
      taskDataList: [],
      pipelineList: [],
      tempPipelineList: [],
      registerPipeline: {
      },
      completedPipeline: {
      },
      completedPlId: "",
      userInfo: {
        userId: "",
        name: "",
        nickName: "",
        phone: "",
      },
      pipelineInfo: [],
      tableShowMode: "", // REGISTER , UPDATE
      tableUpdateFlag: false,
      infoTableUpdateFlag: false,
      collectorTableUpdateFlag: false,
      filterTableUpdateFlag: false,
      convertorTableUpdateFlag: false,
      showRegisterMode: "",
      filterBase: false,
      filterRootKey: "",
      convertDataSet: "",
      registerPipeId: "",
      overlay: false,
      redirectNiFiURL: ""
    };
  },
  mutations: {
    increment(state) {
      state.count++;
    },
  },
});
