<template>
  <div style="width: 95%">
    <div class="pipelineCreateNavBox mgT20">
      <button class="navBoxBtn" @click="moveRoute('defaultInfo')">
        <div class="numberBox">1</div>
        <div>기본정보 입력</div>
      </button>
      <button class="navBoxBtn" @click="moveRoute('collector')">
        <div class="numberBox">2</div>
        <div>데이터 수집</div>
      </button>
      <button class="navBoxBtn" @click="moveRoute('filter')">
        <div class="numberBox">3</div>
        데이터 정제
      </button>
      <button class="navBoxBtn" @click="moveRoute('converter')">
        <div class="numberBox">4</div>
        데이터 변환
      </button>
      <button class="lastbtn" @click="moveRoute('finish')">
        <div class="numberBox">5</div>
        요약 및 등록완료
      </button>
    </div>
    <div>
      <div class="pipelineListTitle">
        <router-view @get-route-contents="getRouteContents"></router-view>
      </div>
    </div>
  </div>
</template>

<script>
import pipelineRegisterService from "../../js/api/pipelineRegister";
export default {
  data() {
    return {
      title: "데이터 파이프라인 기본정보",
      contents: null,
    };
  },
  created() {
    if (this.$route.query.id) {
      pipelineRegisterService
        .getPipelineDraft(this.$route.query.id)
        .then((res) => {
          this.$store.state.pipelineVo = res;
        })
        .catch((error) => error);
    } else {
      pipelineRegisterService
        .getPipelineVo()
        .then((res) => {
          this.$store.state.pipelineVo = res;
        })
        .catch((err) => {
          console.log("PipelinVo 조회에 실패했습니다.", err);
        });
    }
    console.log(this.$store.state.pipelineVo);
    this.moveRoute("defaultInfo");
  },
  methods: {
    moveRoute(name) {
      let contents = null;
      if (name == "defaultInfo") {
        contents = this.defaultInfoContents();
      } else {
        contents = this.$store.state.pipelineVo[name].NifiComponents;
      }

      if (this.$route.name != name) {
        this.$router.push({
          name: name,
          query: { id: this.$route.query.id },
          params: {
            contents: contents,
            convertMode: this.nextRoute(),
            mode: "REGISTER",
          },
        });
      }
    },
    convertMode() {
      console.log("REGISTER");
    },
    defaultInfoContents() {
      return [
        {
          name: "파이프라인 이름",
          inputValue: this.$store.state.pipelineVo.name,
        },
        {
          name: "파이프라인 정의",
          inputValue: this.$store.state.pipelineVo.detail,
        },
      ];
    },
    nextRoute() {
      if (this.$route.name == "defaultInfo") {
        pipelineRegisterService
          .craetePipelineDraft(this.$store.state.pipelineVo)
          .then((res) => {
            console.log(res);
          })
          .catch((err) => {
            console.error(err);
          });
      } else {
        pipelineRegisterService.getPipelineDraft(this.reqParam);
      }
    },
    getRouteContents(value) {
      this.contents = value;
    },
  },
};
</script>
