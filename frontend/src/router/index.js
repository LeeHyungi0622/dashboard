import Vue from "vue";
import VueRouter from "vue-router";
import CustomTable from "@/components/table/CustomTable.vue";

Vue.use(VueRouter);

const routes = [
  {
    path: "/",
    name: "pipelineList",
    component: CustomTable,
  },
  {
    path: "/pipelineCreate",
    name: "pipelineCreate",
    component: CustomTable,
  },
  {
    path: "/pipelineUpdate",
    name: "pipelineUpdate",
    component: CustomTable,
  },
];

const router = new VueRouter({
  mode: "history",
  base: process.env.BASE_URL,
  routes,
});

export default router;
