<template>
  <div class="pipelineUpdateContentBox">
    <div style="justify-content: space-between; display: flex">
      <div class="pipelineUpdateSubTitle fsb16">데이터 정제</div>
      <button class="pipelineUpdateButton" 
      v-if="$store.state.tableShowMode == `UPDATE`"
      @click="changeUpdateFlag"
      >
        {{ $store.state.tableUpdateFlag == "UPDATE" ? "수정완료" : "수정" }}
      </button>
    </div>
    <div v-for="(item, key) in filterData.nifiComponents" :key="key">
      <custom-table :contents="item.requiredProps" />
    </div>
    <div
      v-if="$store.state.tableShowMode == `REGISTER`"
      class="mgT12"
      style="display: flex; justify-content: right"
    >
      <button class="pipelineButton">이전</button>
      <button class="pipelineButton mgL12">임시 저장</button>
      <button
        class="pipelineButton mgL12"
        @click="nextRoute()"
      >
        다음
      </button>
    </div>
  </div>
</template>
<script>
import CustomTable from "./CustomTable.vue";
import collectorService from "../../js/api/collector";
export default {
  components: {
    CustomTable,
  },
  created() {
    this.getFilter();
  },
  data() {
    return {
      filterData: {},
    };
  },
  methods:{
    changeUpdateFlag(){
      this.$store.state.tableUpdateFlag = !this.$store.state.tableUpdateFlag;
    },
    getFilter(){
      if (this.$store.state.tableShowMode == "REGISTER") {
        collectorService
          .getPipelineDraft({
            pipelineid: this.$store.state.registerPipeline.id,
            adaptorName: "filter",
            page: "filter",
          })
          .then((res) => {
            this.$store.state.registerPipeline = res;
            this.filterData =
              this.$store.state.registerPipeline.filter;
          })
          .catch((error) => {
            console.error(error);
          });
      } else {
        collectorService
          .getPipelineComplete({
            adaptorName: "filter",
            pipelineid: this.$store.state.completedPipeline.id,
            page: "filter",
          })
          .then((res) => {
            console.log(res);
            this.$store.state.completedPipeline= res;
            this.filterData =
              this.$store.state.completedPipeline.filter;
          })
          .catch((err) => {
            console.error(err);
          });
      }
    },
    nextRoute(){
      this.$store.state.registerPipeline.filter = this.filterData;
      collectorService
        .postPipelineDraft(this.$store.state.registerPipeline)
        .then((res) => {
          console.log(res);
          this.$store.state.registerPipeline = res;
          this.$store.state.showRegisterMode = 'convertor';
        })
        .catch((err) => {
          console.error(err);
        });
    }
  }
};
</script>
