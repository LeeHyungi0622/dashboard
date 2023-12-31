import Vue from "vue";
import VueRouter from "vue-router";
import pipelineList from "../views/pipelineList/PipelineList.vue";
import pipelineUpdate from "../views/pipelineUpdate/PipelineUpdate.vue";
import PipelineRegister from "../views/pipelineCreate/PipelineRegister.vue";
import PipelineDuplicate from "../views/pipelineCreate/PipelineDuplicate.vue";

Vue.use(VueRouter);

const routes = [
  {
    path: '/',
    name: 'Login',
    component: null,
    redirect: "/list",
  },
  {
    path: '*',
    component: {
      template: '<script>alert("잘못된 호출입니다.");</script>'
    }
  },
  {
    path: "/list",
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
    path: "/pipelineRegister/duplicate/:itemId",
    name: "pipelineDuplicate",
    component: PipelineDuplicate,
    props: true,
  },
  {
    path: "/pipelineUpdate/:itemId",
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
