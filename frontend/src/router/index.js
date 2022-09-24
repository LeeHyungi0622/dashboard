import Vue from "vue";
import VueRouter from "vue-router";
import pipelineList from "../views/pipelineList/PipelineList.vue";
import pipelineUpdate from "../views/pipelineUpdate/PipelineUpdate.vue";
import pipelineCreate from "../views/pipelineCreate/PipelineCreate.vue";
import defaultInfo from "../components/pipeline/DefaultInfo";
import dataCollect from "../components/pipeline/DataCollect";
import dataConvert from "../components/pipeline/DataConvert";
import dataRefine from "../components/pipeline/DataRefine";

Vue.use(VueRouter);

const routes = [
  {
    path: "/",
    name: "pipelineList",
    component: pipelineList,
  },
  {
    path: "/pipelineCreate",
    name: "pipelineCreate",
    component: pipelineCreate,
    children: [
      {
        path: "/",
        name: "defaultInfo",
        component: defaultInfo,
        props: (route) => ({
          contents: route.params.contents,
          convertMode: route.params.convertMode,
          mode: route.params.mode,
        }),
      },
      {
        path: "/collect",
        name: "collect",
        component: dataCollect,
        props: (route) => ({
          contents: route.params.contents,
          convertMode: route.params.convertMode,
          mode: route.params.mode,
        }),
      },
      {
        path: "/convert",
        name: "convert",
        component: dataConvert,
        props: (route) => ({
          contents: route.params.contents,
          convertMode: route.params.convertMode,
          mode: route.params.mode,
        }),
      },
      {
        path: "/refine",
        name: "refine",
        component: dataRefine,
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
