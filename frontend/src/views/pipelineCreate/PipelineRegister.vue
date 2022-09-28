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
        <router-view></router-view>
        <div
          class="mgT12"
          style="display: flex; justify-content: right; width: 95%"
        >
          <button class="pipelineButton">이전</button>
          <button class="pipelineButton mgL12">임시 저장</button>
          <button class="pipelineButton mgL12">다음</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
// import devData from "../../json/devData.json";
import devData from "../../json/pipelineCreate.json";
export default {
  data() {
    return {
      title: "데이터 파이프라인 기본정보",
      devData: devData,
    };
  },
  created() {
    this.moveRoute("defaultInfo");
    console.log(this.$store.state);
  },
  methods: {
    moveRoute(name) {
      let contents = null;
      if (name == "defaultInfo") {
        contents = this.defaultInfoContents();
      } else {
        contents = devData[name].NifiComponents;
      }

      if (this.$route.name != name) {
        this.$router.push({
          name: name,
          params: {
            contents: contents,
            convertMode: this.convertMode(),
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
          inputValue: this.devData.name,
        },
        {
          name: "파이프라인 정의",
          inputValue: this.devData.detail,
        },
      ];
    },
  },
};
</script>
