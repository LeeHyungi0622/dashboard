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
    this.moveRoute("defaultInfo");
    if (this.$route.query.id) {
      console.log("sdasdsadas");
      pipelineRegisterService
        .getPipelineDraft(this.$route.query.id)
        .then((res) => {
          this.$store.state.pipelineVo = res;
        })
        .catch((err) =>
          console.error("임시저장 Pipeline 조회에 실패했습니다.", err)
        );
    } else {
      pipelineRegisterService
        .getPipelineVo()
        .then((res) => {
          this.$store.state.pipelineVo = res;
        })
        .catch((err) => {
          console.error("PipelinVo 조회에 실패했습니다.", err);
        });
    }
  },
  methods: {
    moveRoute(name) {
      if (this.$route.name != name && this.$route.query.id) {
        this.$router.push({
          name: name,
          query: { id: this.$route.query.id },
          params: {
            convertMode: this.convertMode,
            mode: "REGISTER",
          },
        });
      } else if (this.$route.name != name) {
        this.$router.push({
          name: name,
          params: {
            convertMode: this.convertMode,
            mode: "REGISTER",
          },
        });
      }
    },
    convertMode() {
      console.log("REGISTER");
    },
    nextRoute() {
      pipelineRegisterService.getPipelineDraft(this.reqParam);
    },
    getRouteContents(value) {
      this.contents = value;
    },
  },
};
</script>
