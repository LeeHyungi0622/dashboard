import Vue from "vue";
import VueRouter from "vue-router";
import pipelineList from "../views/pipelineList/PipelineList.vue";
import pipelineUpdate from "../views/pipelineUpdate/PipelineUpdate.vue";
import PipelineRegister from "../views/pipelineCreate/PipelineRegister.vue";
import defaultInfo from "../components/pipeline/DefaultInfo";
import dataCollect from "../components/pipeline/DataCollect";
import dataConvert from "../components/pipeline/DataConvert";
import datafilters from "../components/pipeline/DataFilters";

Vue.use(VueRouter);

const routes = [
  {
    path: "/",
    name: "pipelineList",
    component: pipelineList,
  },
  {
    path: "/pipelineRegister",
    name: "pipelineRegister",
    component: PipelineRegister,
    props: true,
    children: [
      {
        path: "/defaultInfo",
        name: "defaultInfo",
        component: defaultInfo,
        props: (route) => ({
          contents: route.params.contents,
          convertMode: route.params.convertMode,
          mode: route.params.mode,
          id: route.params.id,
        }),
      },
      {
        path: "/collector",
        name: "collector",
        component: dataCollect,
        props: (route) => ({
          contents: route.params.contents,
          convertMode: route.params.convertMode,
          mode: route.params.mode,
        }),
      },
      {
        path: "/converter",
        name: "converter",
        component: dataConvert,
        props: (route) => ({
          contents: route.params.contents,
          convertMode: route.params.convertMode,
          mode: route.params.mode,
        }),
      },
      {
        path: "/filter",
        name: "filter",
        component: datafilters,
        props: (route) => ({
          contents: route.params.contents,
          convertMode: route.params.convertMode,
          mode: route.params.mode,
        }),
      },
    ],
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
