<template>
  <div class="pipelineUpdateContentBox">
    <div style="justify-content: space-between; display: flex">
      <div class="pipelineUpdateMainTitle fsb16">데이터 변환</div>
      <button class="pipelineUpdateButton" 
      v-if="$store.state.tableShowMode == `UPDATE`"
      @click="changeUpdateFlag"
      >
        {{ $store.state.convertorTableUpdateFlag ? "수정완료" : "수정" }}
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
              @change="callConvertorProps($event)"
              :disabled="!$store.state.convertorTableUpdateFlag"
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
          v-if="$store.state.convertorTableUpdateFlag || $store.state.tableShowMode == 'REGISTER'"
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
          v-if="$store.state.convertorTableUpdateFlag || $store.state.tableShowMode == 'REGISTER'"
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
    if(this.$store.state.tableShowMode == `UPDATE`){
      this.getPipeline = this.$store.state.completedPipeline;
      this.selectedConverterValue = this.getPipeline.dataSet;
      this.convProps = this.convertDataSetProps(this.getPipeline.converter);
      console.log(this.convProps);
      this.convId = this.convertId(this.getPipeline.converter);
      console.log(this.convId);
    }
    else{
      this.getPipeline = this.$store.state.registerPipeline;
      if (this.getPipeline.converter != null) {
        this.selectedConverterValue = this.getPipeline.dataSet;
        this.convProps = this.convertDataSetProps(this.getPipeline.converter);
        this.convId = this.convertId(this.getPipeline.converter);
      }
    }
  },
  computed: {
    generationKey() {
      if(this.$store.state.tableShowMode == `UPDATE`){
        let last = "";
        if(this.convId != null){
          for(var relevel of this.convId){
            if(relevel.inputValue != null){
              last = last + ":${"+relevel.inputValue+"}";
            }
          }
        }
        return "Generated key : urn:datahub:"+ this.getPipeline.dataModel + last;
      }
      else{
        let last = "";
        if(this.convId != null){
          for(var level of this.convId){
            if(level.inputValue != null){
              last = last + ":${"+level.inputValue+"}";
            }
          }
        }
        return "Generated key : urn:datahub:"+ this.getPipeline.dataModel + last;
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
      selectedConverterValue: {},
      convProps: [],
      convId: [],
      rawDataSetProps: {},
      rawIdNifi: {},
      getPipeline: {}
    };
  },
  methods: {
    convertToProps(){
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
    },
    convertToId(){
      if(this.convId != null){
        for(var id of this.convId){
              var nifiPropList = [];
              for(var nifiProp of this.rawIdNifi.requiredProps){
                if(nifiProp.name == id.name){
                  nifiProp.inputValue = id.inputValue;
                  nifiPropList.push({name:nifiProp.name,detail:id.detail,inputValue:id.inputValue});
                }
              }
              this.rawIdNifi.requiredProps = nifiPropList;
          }
      }
    },
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
          this.rawIdNifi = nifi;
          this.convId = nifi.requiredProps;
        }
      }
    },
    changeUpdateFlag(){
      this.$store.state.convertorTableUpdateFlag = !this.$store.state.convertorTableUpdateFlag;
    },
    callConvertorProps(event){
      if(this.$store.state.tableShowMode == `UPDATE`){
        this.completedContents(event.target.value);
      }
      else{
        this.registerContents(event.target.value);
      }
    },
    completedContents(val){
      collectorService
          .getPipelineComplete({
            adaptorName: "converter",
            pipelineid: this.getPipeline.id,
            page: "converter",
            datasetid: val
          })
          .then((res) => {
            this.getPipeline = res;
            this.convProps = this.convertDataSetProps(this.getPipeline.converter);
            this.convertId(this.getPipeline.converter);
          })
          .catch((err) => {
            console.error(err);
          });
    },
    registerContents(val){
      collectorService
          .getPipelineDraft({
            pipelineid: this.getPipeline.id,
            adaptorName: "converter",
            page: "converter",
            datasetid: val
          })
          .then((res) => {
            this.getPipeline = res;
            this.convProps = this.convertDataSetProps(this.getPipeline.converter);
            this.convertId(this.getPipeline.converter);
          })
          .catch((error) => {
            console.error(error);
          });
    },
    getDataSet(){
      dataSetService.getDataSet()
        .then((res)=>{
          this.dataSetList = res;
        })
        .catch((error) => {
            console.error(error);
          });
    },
    nextRoute(){
      this.convertToProps();
      this.convertToId();
      this.$store.state.registerPipeId = this.generationKey;
      var convertNifi = []
      convertNifi.push(this.rawDataSetProps);
      convertNifi.push(this.rawIdNifi);
      this.getPipeline.converter.nifiComponents = convertNifi;
      this.$store.state.registerPipeline = this.getPipeline;
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
      this.convertToProps();
      this.convertToId();
      this.$store.state.registerPipeId = this.generationKey;
      var convertNifi = []
      convertNifi.push(this.rawDataSetProps);
      convertNifi.push(this.rawIdNifi);
      this.getPipeline.converter.nifiComponents = convertNifi;
      this.$store.state.registerPipeline = this.getPipeline;
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
      this.convertToProps();
      this.convertToId();
      this.$store.state.registerPipeId = this.generationKey;
      var convertNifi = []
      convertNifi.push(this.rawDataSetProps);
      convertNifi.push(this.rawIdNifi);
      this.getPipeline.converter.nifiComponents = convertNifi;
      this.$store.state.registerPipeline = this.getPipeline;
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
