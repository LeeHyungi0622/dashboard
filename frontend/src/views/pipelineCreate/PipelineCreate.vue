<template>
  <div style="width: 95%">
    <div class="pipelineCreateNavBox mgT20">
      <button class="navBoxBtn" @click="moveRoute('defaultInfo')">
        <div class="numberBox">1</div>
        <div>기본정보 입력</div>
      </button>
      <button class="navBoxBtn" @click="moveRoute('collect')">
        <div class="numberBox">2</div>
        <div>데이터 수집</div>
      </button>
      <button class="navBoxBtn" @click="moveRoute('refine')">
        <div class="numberBox">3</div>
        데이터 정제
      </button>
      <button class="navBoxBtn" @click="moveRoute('convert')">
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
        <router-view></router-view>
      </div>
    </div>
  </div>
</template>

<script>
import devData from "../../json/devData.json";
export default {
  data() {
    return {
      title: "데이터 파이프라인 기본정보",
      devData: devData,
    };
  },
  methods: {
    moveRoute(name) {
      if (this.activationRoutePath != name) {
        this.$router.push({
          name: name,
          params: {
            contents: devData[name],
            convertMode: "REGISTER",
            mode: "REGISTER",
          },
        });
      }
    },
  },
};
</script>
