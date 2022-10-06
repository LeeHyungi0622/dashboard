<template>
  <div style="width: 95%">
    <div class="pipelineCreateNavBox mgT20">
      <button class="navBoxBtn" @click="actionFilter()">
        <div class="numberBox">1</div>
        <div>기본정보 입력</div>
      </button>
      <button class="navBoxBtn">
        <div class="numberBox">2</div>
        <div>데이터 수집</div>
      </button>
      <button class="navBoxBtn">
        <div class="numberBox">3</div>
        데이터 정제
      </button>
      <button class="navBoxBtn">
        <div class="numberBox">4</div>
        데이터 변환
      </button>
      <button class="lastbtn">
        <div class="numberBox">5</div>
        요약 및 등록완료
      </button>
    </div>
    <div>
      <div class="pipelineListTitle">
        <default-info />
        <data-collect
        />
        <data-filters
        />
        <data-convert
        />
        <!-- <router-view @get-route-contents="getRouteContents"></router-view> -->
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
      showModule: "",
    };
  },
  created() {
    if (this.$store.state.pipelineVo.id) {
      pipelineRegisterService
      .getPipelineDraft(this.$store.state.pipelineVo.id)
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
      if (this.$route.name != name) {
        this.$router.push({
          name: name
        });
      }
    },
    convertMode(val) {
      this.showModule = val;
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
