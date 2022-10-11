import Vue from "vue";
import VueRouter from "vue-router";
import pipelineList from "../views/pipelineList/PipelineList.vue";
import pipelineUpdate from "../views/pipelineUpdate/PipelineUpdate.vue";
import PipelineRegister from "../views/pipelineCreate/PipelineRegister.vue";

Vue.use(VueRouter);

const routes = [
  {
    path: "/",
    name: "pipelineList",
    component: pipelineList,
  },
  {
    path: "/pipelineRegister/:itemId",
    name: "pipelineRegister",
    component: PipelineRegister,
    props: true,
  },
  {
    path: "/pipelineRegister",
    name: "pipelineRegister",
    component: PipelineRegister,
    props: true,
  },
  {
    path: "/pipelineUpdate",
    name: "pipelineUpdate",
    component: pipelineUpdate,
    props: true,
  },
];

const router = new VueRouter({
  mode: "history",
  base: process.env.BASE_URL,
  routes,
});

export default router;
