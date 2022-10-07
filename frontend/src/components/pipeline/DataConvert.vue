<template>
  <div class="pipelineUpdateContentBox">
    <div style="justify-content: space-between; display: flex">
      <div class="pipelineUpdateMainTitle fsb16">데이터 변환</div>
      <button class="pipelineUpdateButton" 
      v-if="$store.state.tableShowMode == `UPDATE`"
      @click="changeUpdateFlag"
      >
        {{ $store.state.tableUpdateFlag == "UPDATE" ? "수정완료" : "수정" }}
      </button>
    </div>
    <div class="customTableMainArea">
      <div class="customTable">
        <div class="header fsb12">
          <p>dataSet</p>
        </div>
        <div class="value">
          <div>
            <select
              style="padding: 0px 20px 0px 20px"
              v-model="selectedConverterValue"
            >
              <option
                v-for="(item, key) in dataSetList.dataSetId"
                :key="key"
                :value="item"
              >
                {{ item }}
              </option>
            </select>
          </div>
        </div>
      </div>
    </div>

    <v-data-table
      :headers="convertHeaders"
      :items="convProps"
      class="pipelineUpdateConvertVFT"
      :hide-default-footer="true"
      style="text-align: center"
      ><template v-slot:[`item.inputValue`]="{ item }">
        <input
          v-if="$store.state.tableShowMode == 'UPDATE' || $store.state.tableShowMode == 'REGISTER'"
          v-model="item.inputValue"
        />
        <div style="padding-left: 10px" v-else>{{ item.inputValue }}</div>
      </template>
    </v-data-table>

    <div class="pipelineUpdateSubTitle fsb14">ID 생성 규칙 설정</div>
    <div class="customTableBox fsb12">{{ generationKey }}</div>

    <v-data-table
      :headers="IdHeaders"
      :items="convId"
      class="pipelineUpdateIdVFT"
      :hide-default-footer="true"
      style="text-align: center"
    >
      <template v-slot:[`item.inputValue`]="{ item }">
        <input
          v-if="$store.state.tableShowMode == 'UPDATE' || $store.state.tableShowMode == 'REGISTER'"
          v-model="item.inputValue"
        />
        <div style="padding-left: 10px" v-else>{{ item.inputValue }}</div>
      </template>
    </v-data-table>
    <div
      v-if="$store.state.tableShowMode == `REGISTER`"
      class="mgT12"
      style="display: flex; justify-content: right"
    >
      <button class="pipelineButton" @click="beforeRoute()" >이전</button>
      <button class="pipelineButton mgL12" @click="saveDraft()" >임시 저장</button>
      <button class="pipelineButton mgL12" @click="nextRoute()">다음</button>
    </div>
  </div>
</template>

<script>
import collectorService from "../../js/api/collector";
import dataSetService from "../../js/api/dataSet";
export default {
  created() {
    this.getDataSet();
  },
  computed: {
    generationKey() {
      let last = "";
      if(this.convId != null){
        for(var level of this.convId){
          if(level.inputValue != null){
            last = last + ":${"+level.inputValue+"}";
          }
        }
      }
      return "Generated key : urn:datahub:"+ this.$store.state.registerPipeline.dataModel + last;
    },

  },
  watch: {
    selectedConverterValue(){
      if (this.$store.state.tableShowMode == "REGISTER") {
        collectorService
          .getPipelineDraft({
            pipelineid: this.$store.state.registerPipeline.id,
            adaptorName: "converter",
            page: "CONVERTER",
            datasetid: this.selectedConverterValue
          })
          .then((res) => {
            this.$store.state.registerPipeline = res;
            this.converterData = 
            this.$store.state.registerPipeline.converter;
            this.convProps = this.convertDataSetProps(this.converterData);
            this.convertId(this.converterData);
          })
          .catch((error) => {
            console.error(error);
          });
      } else {
        collectorService
          .getPipelineComplete({
            adaptorName: "converter",
            pipelineid: this.$store.state.completedPipeline.id,
            page: "CONVERTER",
            datasetid: this.selectedConverterValue
          })
          .then((res) => {
            this.$store.state.completedPipeline= res;
            this.converterData =
              this.$store.state.completedPipeline.converter;
            this.convProps = this.convertDataSetProps(this.converterData);
            this.convertId(this.converterData);
          })
          .catch((err) => {
            console.error(err);
          });
      }
    },
    convProps(){
      if(this.convProps != null){
        for(var prop of this.convProps){
              var nifiPropList = [];
              for(var nifiProp of this.rawDataSetProps.requiredProps){
                if(nifiProp.name == prop.name.split('(')[0]){
                  nifiProp.inputValue = prop.inputValue;
                  nifiPropList.push({name:nifiProp.name,detail:prop.detail,inputValue:prop.inputValue});
                }
              }
              this.rawDataSetProps.requiredProps = nifiPropList;
          }

      }
    }
  },
  
  data() {
    return {
      convertHeaders: [
        {
          text: "DataSet 속성 (속성 유형)",
          sortable: false,
          value: "name",
        },
        {
          text: "Raw Data 속성",
          value: "inputValue",
          sortable: false,
        },
      ],
      IdHeaders: [
        {
          text: "Depth",
          sortable: false,
          value: "name",
        },
        { text: "ID Key", value: "inputValue", sortable: false },
      ],
      dataSetList: [],
      converterData: {
        nifiComponents: [],
      },
      selectedConverterValue: {},
      convProps: [],
      convId: [],
      rawDataSetProps: {}
    };
  },
  methods: {
    convertDataSetProps(data){
      var convertProps = [];
      for(var nifi of data.nifiComponents){
        if(nifi.name == 'DataSetProps'){
          this.rawDataSetProps = nifi;
          this.rawDataSetProps.requiredProps = nifi.requiredProps;
          for(var prop of nifi.requiredProps){
            const name = prop.name + "(" + prop.detail + ")";
            const inputValue = "";
            const convertProp = {name : name, detail:prop.detail ,inputValue: inputValue};
            convertProps.push(convertProp);
          }
        }
      }
      return convertProps;
    },
    convertId(data){
      for(var nifi of data.nifiComponents){
        if(nifi.name == 'IDGenerater'){
          this.convId = nifi.requiredProps;
        }
      }
    },
    changeUpdateFlag(){
      this.$store.state.tableUpdateFlag = !this.$store.state.tableUpdateFlag;
    },
    getDataSet(){
      dataSetService.getDataSet()
        .then((res)=>{
          console.log(res);
          this.dataSetList = res;
        })
        .catch((error) => {
            console.error(error);
          });
    },
    nextRoute(){
      this.$store.state.registerPipeId = this.generationKey;
      // this.$store.state.registerPipeline.collector = this.collectorData;
      collectorService
        .postPipelineDraft(this.$store.state.registerPipeline)
        .then((res) => {
          console.log(res);
          this.$store.state.registerPipeline = res;
          this.$store.state.showRegisterMode = 'complete';
        })
        .catch((err) => {
          console.error(err);
        });
    },
    beforeRoute(){
      this.$store.state.registerPipeId = this.generationKey;
      // this.$store.state.registerPipeline.collector = this.collectorData;
      collectorService
        .postPipelineDraft(this.$store.state.registerPipeline)
        .then((res) => {
          console.log(res);
          this.$store.state.registerPipeline = res;
          this.$store.state.showRegisterMode = 'filter';
        })
        .catch((err) => {
          console.error(err);
        });
    },
    saveDraft(){
      this.$store.state.registerPipeId = this.generationKey;
      this.$store.state.registerPipeline.collector = this.collectorData;
      collectorService
        .postPipelineDraft(this.$store.state.registerPipeline)
        .then((res) => {
          console.log(res);
          this.$store.state.registerPipeline = res;
        })
        .catch((err) => {
          console.error(err);
        });
    }
  },
};
</script>
