import Vue from "vue";
import Vuex from "vuex";

Vue.use(Vuex);
// Create a new store instance.
export const store = new Vuex.Store({
  state() {
    return {
      pipelineList: [],
      pipelineVo: {},
      userInfo: {
        userId: "cityhub10",
        name: "홍길동",
        nickName: "홍길동",
        phone: "010-1234-5678",
      },
    };
  },
  mutations: {
    increment(state) {
      state.count++;
    },
  },
});
