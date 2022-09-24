import Vue from "vue";
import App from "./App.vue";
import Axios from "axios";
import router from "./router";
import vuetify from "@/plugins/vuetify.js";
import css from "@/assets/css/common.css";

const ws = new WebSocket("ws://192.168.1.95:8099/webpipeline");

Vue.config.productionTip = false;
Vue.prototype.$axios = Axios;
Vue.use(vuetify);
Vue.prototype.$ws = ws;

new Vue({
  css,
  router,
  vuetify,
  render: (h) => h(App),
}).$mount("#app");
