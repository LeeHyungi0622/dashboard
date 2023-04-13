<template>
  <div>
    <div class="pipelineListTitle">
      <p class="fsb16">데이터 파이프라인 상세/수정</p>
    </div>
    <div style="margin: 3%">
      <default-info
        v-if="$store.state.completedPipeline.name"
      />
      <data-collect
        v-if="$store.state.completedPipeline['collector']"
        :contents="getContents('collector')"
      />
      <data-filters
        v-if="$store.state.completedPipeline['filter']"
        :contents="getContents('filter')"
      />
      <data-convert
        v-if="$store.state.completedPipeline['converter']"
        :contents="getContents('converter')"
      />
      <div class="pipelineUpdateBtnBox mgT12">
        <button class="pipelineUpdateButton" @click="goPipelineList()">
          목록으로
        </button>
        <button class="pipelineUpdateButton mgL12" @click="updatePipeline()">저장</button>
      </div>
    </div>
    <Footer/>
  </div>
</template>
<script>
import DefaultInfo from "../../components/pipeline/DefaultInfo.vue";
import DataCollect from "../../components/pipeline/DataCollect.vue";
import DataFilters from "../../components/pipeline/DataFilters.vue";
import DataConvert from "../../components/pipeline/DataConvert.vue";
import pipelineUpdateService from "../../js/api/pipelineUpdate";
import EventBus from "@/eventBus/EventBus.js";
export default {
  components: {
    DataConvert,
    DefaultInfo,
    DataCollect,
    DataFilters,
  },
  
  created(){
    this.$store.state.tableShowMode = "UPDATE";
    pipelineUpdateService
      .getPipelineListById(this.itemId)
      .then((res) => {
        this.$store.state.completedPipeline = res;
      })
      .catch((err) => {
        console.log("PipelinListById 조회에 실패했습니다.", err);
      });
    this.$store.state.infoTableUpdateFlag = false;
    this.$store.state.collectorTableUpdateFlag = false;
    this.$store.state.filterTableUpdateFlag = false;
    this.$store.state.convertorTableUpdateFlag = false;

  },
  data() {
    return {
      selectedDataSet: "",
    };
  },
  props: {
    itemId: String
  },
  methods: {
    selectedDataSetFunction(value) {
      this.selectedDataSet = value;
    },
    getContents(contentsName) {
      return this.$store.state.completedPipeline[contentsName].NifiComponents;
    },

    goPipelineList() {
      this.$router.push({
        name: "pipelineList",
      });
    },
    updatePipeline(){
      if(this.$store.state.completedPipeline.status == "STOPPED"){
        this.$store.state.overlay = true;
        pipelineUpdateService
        .updateConpletedPipeline(this.$store.state.completedPipeline.id,this.$store.state.completedPipeline).then(()=>{
          this.$store.state.overlay = false;
          let alertPayload = {
            title: "저장 완료",
            text:
            this.$store.state.completedPipeline.name +
              " 파이프라인의 " +
              "<br/>수정이 완료되었습니다.",
            url: "completedUpdate",
          };
          this.$router.push({
              name: "pipelineList",
            });
          EventBus.$emit("show-alert-popup", alertPayload);
        })
        .catch((err) => {
          console.log("Update Pipeline에 실패했습니다.", err);
        });
      }
      else{
        let alertPayload = {
          title: "수정 실패",
          text:
          this.$store.state.completedPipeline.name +
            " 파이프라인의 상태가 " + this.$store.state.completedPipeline.status + "이므로" +
            "<br/>정지 완료 후 수정이 가능합니다.",
          url: "completedUpdate",
        };
        this.$router.push({
            name: "pipelineList",
          });
        EventBus.$emit("show-alert-popup", alertPayload);
      }
    },
  },
};
</script>
