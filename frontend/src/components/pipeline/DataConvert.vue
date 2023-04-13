<template>
  <div class="pipelineUpdateContentBox">
    <div style="justify-content: space-between; display: flex">
      <div class="pipelineUpdateMainTitle fsb16">데이터 변환</div>
      <button class="pipelineUpdateButton" 
      v-if="$store.state.tableShowMode == `UPDATE`"
      @click="changeUpdateFlag" 
      :disabled="!isCompleted[0]"
      >
        {{ $store.state.convertorTableUpdateFlag ? "수정완료" : "수정" }}
      </button>
    </div>
    <div class="customTableMainArea" v-if="$store.state.tableShowMode != `UPDATE`">
      <div class="customTable">
        <div class="header fsb12">
          <p>Root Key</p>
        </div>
        <div class="value ">
          <div style="padding: 0px 20px 0px 20px">
            {{ $store.state.filterRootKey }}
          </div>
        </div>
      </div>
    </div>
    <div class="pipelineUpdateSubTitle fsb14">원천 데이터 변환 규칙 설정</div>
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
      :items-per-page="convProps.length + 1"
      style="text-align: center"
      ><template v-slot:[`item.inputValue`]="{ item }">
        <input
          v-if="$store.state.convertorTableUpdateFlag || $store.state.tableShowMode == 'REGISTER'"
          v-model="item.inputValue"
          maxlength="300"
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
          maxlength="300"
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
      <button class="pipelineButton mgL12" @click="nextRoute()" :disabled="!isCompleted[0]">다음</button>
    </div>
  </div>
</template>

<script>
import collectorService from "../../js/api/collector";
import dataSetService from "../../js/api/dataSet";
import EventBus from "@/eventBus/EventBus.js";
export default {
  created() {
    this.getDataSet();
    if(this.$store.state.tableShowMode == `UPDATE`){
      this.getPipeline = this.$store.state.completedPipeline;
      this.selectedConverterValue = this.getPipeline.dataSet;
      this.convProps = this.convertDataSetProps(this.getPipeline.converter);
      this.convertId(this.getPipeline.converter);
      //store에도 반영하는 로직 추가
      this.convertToProps();
      this.convertToId();
      let convertNifi = []
      convertNifi.push(this.rawDataSetProps);
      convertNifi.push(this.rawIdNifi);
      this.getPipeline.converter.nifiComponents = convertNifi;
      this.$store.state.completedPipeline = this.getPipeline;
    }
    else{
      this.getPipeline = this.$store.state.registerPipeline;
      if (this.getPipeline.converter != null) {
        this.selectedConverterValue = this.getPipeline.dataSet;
        this.convProps = this.convertDataSetProps(this.getPipeline.converter);
        this.convertId(this.getPipeline.converter);
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
              if(relevel.inputValue != ""){
                last = last + ":${"+relevel.inputValue+"}";
              }
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
              if(level.inputValue != ""){
                last = last + ":${"+level.inputValue+"}";
              }
            }
          }
        }
        return "Generated key : urn:datahub:"+ this.getPipeline.dataModel + last;
      }
    },
    isCompleted(){
      if(this.convProps.length != 0 && this.convId.length != 0){
        for(let prop of this.convProps){
          if(prop.inputValue == null || prop.inputValue == ""|| prop.inputValue.replace(/^\s+|\s+$/g, '')==""){
            return [false,"blank"];
          }
        }
        for(let prop of this.convId){
          if(prop.name == "level1"){
            if(prop.inputValue == null || prop.inputValue == ""|| prop.inputValue.replace(/^\s+|\s+$/g, '')==""){
              return  [false,"level"];
            }
          }
        }
        return [true, null, null];
      }
      return [false,"Not Found",null];
    },
    isVaild(){
      let isOkProps = false;
      let isOkId = false;
      if(this.convProps.length != 0 && this.convId.length != 0){
        for(let prop of this.convProps){
          if(prop.detail == "Date Format" || prop.detail == "unitCode"){
            continue;
          }
          else{
            if(prop.inputValue == null) {
              return [false, prop];
            }
            else if(!prop.inputValue.includes("\"") || prop.inputValue.includes(" ") || prop.inputValue.includes("\"\"")){
              return [false, prop];
            }
            else{
              if((prop.inputValue.split("\"").length - 1)%2 != 0){
                return [false, prop];
              }
              else{
                for(let e = 0; e < prop.inputValue.split("\"").length; e+=2){
                  if(e == 0 || e == prop.inputValue.split("\"").length-1){
                    if(prop.inputValue.split("\"")[e] != ""){
                      return [false, prop];
                    }
                  }
                  else{
                    if(prop.inputValue.split("\"")[e] != "."){
                      return [false, prop];
                    }
                  }
                }
                isOkProps = true;
              }
            }
          }
        }
        for(let id of this.convId){
          if(id.inputValue){
            if(id.inputValue == null) {
              return [false, id];
            }
            if(!id.inputValue.includes("\"") || id.inputValue.includes(" ") || id.inputValue.includes("\"\"")){
              return [false, id];
            }
            else{
              if((id.inputValue.split("\"").length - 1)%2 != 0){
                return [false, id];
              }
              else{
                for(let e = 0; e < id.inputValue.split("\"").length; e+=2){
                if(e == 0 || e == id.inputValue.split("\"").length-1){
                  if(id.inputValue.split("\"")[e] != ""){
                    return [false, id];
                  }
                }
                else{
                  if(id.inputValue.split("\"")[e] != "."){
                    return [false, id];
                  }
                }
              }
                isOkId = true;
              }
            }
          }
        }
        if(isOkProps && isOkId) return [true, null];
      }
      return [false, "not found"];
    },
    isTempVaild(){
      if(this.convProps.length != 0 && this.convId.length != 0){
        for(let prop of this.convProps){
          if(prop.inputValue == null) {
              return [false, prop];
            }
          if(prop.inputValue.replace(/^\s+|\s+$/g, '')=="") return [false,prop];
        }
        for(let id of this.convId){
          if(id.name == "level1" && id.inputValue == null) {
              return [false, id];
            }
          if(id.inputValue.replace(/^\s+|\s+$/g, '')=="") return [false,"level"];
        }
      }
      return [true, null];
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
      rawDataSetProps: {
        requiredProps: []
      },
      rawIdNifi: {
        requiredProps: []
      },
      getPipeline: {},
      selectDataModel: ""
    };
  },
  methods: {
    convertToProps(){
      if(this.convProps != null){
        for(let nifiProp of this.rawDataSetProps.requiredProps){
          for(let prop of this.convProps){
            if(nifiProp.name == prop.name.split('(')[0] && nifiProp.detail == prop.detail){
                nifiProp.inputValue = prop.inputValue;
            }
          }
        }
      }
    },
    convertToId(){
      if(this.convId != null){
        for(let nifiProp of this.rawIdNifi.requiredProps){
          for(let id of this.convId){
            if(nifiProp.name == id.name){
              nifiProp.inputValue = id.inputValue;
            }
          }
        }
      }
    },
    convertDataSetProps(data){
      let convertProps = [];
      for(let nifi of data.nifiComponents){
        if(nifi.name == 'DataSetProps'){
          this.rawDataSetProps = nifi;
          this.rawDataSetProps.requiredProps = nifi.requiredProps;
          for(let prop of nifi.requiredProps){
            const name = prop.name + "(" + prop.detail + ")";
            let inputValue;
            if(prop.detail == "Date Format"){
              if(prop.inputValue == null || prop.inputValue == ""){
                inputValue = "";
              } else if(this.$store.state.tableShowMode == `UPDATE`){
                inputValue = prop.inputValue.split("\"")[1];
              } else{
                inputValue = prop.inputValue;
              }
              }
              else{
                inputValue = prop.inputValue;
              }
            const convertProp = {name : name, detail:prop.detail , inputValue: inputValue};
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
      if(this.isVaild[0]){
        this.$store.state.convertorTableUpdateFlag = !this.$store.state.convertorTableUpdateFlag;
        this.convertToProps();
        this.convertToId();
        let convertNifi = []
        convertNifi.push(this.rawDataSetProps);
        convertNifi.push(this.rawIdNifi);
        this.getPipeline.converter.nifiComponents = convertNifi;
        this.$store.state.completedPipeline = this.getPipeline;
      } else{
        let alertPayload = {
          title: "입력 값 오류",
          text:
            this.isVaild[1].name + " 입력 값에 오류가 있습니다. " +
            "<br/>구분자(.[온점] 또는 \"[쌍따옴표]) 혹은 공백을 확인해 주십시오.",
          url: "not Vaild",
        };
        this.$store.state.overlay = false;
        EventBus.$emit("show-alert-popup", alertPayload);
      }
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
            id: this.getPipeline.id,
            page: "converter",
            datasetid: val
          })
          .then((res) => {
            this.$store.state.completedPipeline = res;
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
            this.$store.state.registerPipeline = res;
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
      this.$store.state.overlay = true;
      if(this.isVaild[0]){
        this.convertToProps();
        this.convertToId();
        this.$store.state.registerPipeId = this.generationKey.split(" ")[3];
        this.$store.state.convertDataSet = this.selectedConverterValue;
        let convertNifi = []
        convertNifi.push(this.rawDataSetProps);
        convertNifi.push(this.rawIdNifi);
        this.getPipeline.converter.nifiComponents = convertNifi;
        this.$store.state.registerPipeline = this.getPipeline;
        collectorService
          .postPipelineDraft(this.$store.state.registerPipeline)
          .then((res) => {
            this.$store.state.registerPipeline = res;
            this.$store.state.showRegisterMode = 'complete';
            this.$store.state.overlay = false;
          })
          .catch((err) => {
            console.error(err);
          });
      }
      else{
        if(this.isVaild[1] == "not found"){
            let alertPayload = {
            title: "입력 값 오류",
            text:
              " DataSet 선택이 필요합니다. " +
              "<br/>DataSet 목록 중 하나를 선택해주십시오.",
            url: "not Vaild",
          };
          this.$store.state.overlay = false;
          EventBus.$emit("show-alert-popup", alertPayload);
        } else{
          let alertPayload = {
            title: "입력 값 오류",
            text:
              this.isVaild[1].name + " 입력 값에 오류가 있습니다. " +
              "<br/>구분자(.[온점] 또는 \"[쌍따옴표]) 혹은 공백을 확인해 주십시오.",
            url: "not Vaild",
          };
          this.$store.state.overlay = false;
          EventBus.$emit("show-alert-popup", alertPayload);
        }
      }
    },
    beforeRoute(){
      this.$store.state.overlay = true;
        if(this.rawDataSetProps.requiredProps.length != 0){
          this.convertToProps();
          this.convertToId();
          let convertNifi = []
          convertNifi.push(this.rawDataSetProps);
          convertNifi.push(this.rawIdNifi);
          this.getPipeline.converter.nifiComponents = convertNifi;
        }
          this.$store.state.registerPipeId = this.generationKey.split(":")[1];
        this.$store.state.registerPipeline = this.getPipeline;
        collectorService
          .postPipelineDraft(this.$store.state.registerPipeline)
          .then((res) => {
            this.$store.state.registerPipeline = res;
            this.$store.state.showRegisterMode = 'filter';
            this.$store.state.overlay = false;
          })
          .catch((err) => {
            console.error(err);
          });
        
    },
    saveDraft(){
      this.$store.state.overlay = true;

        if(this.rawDataSetProps.requiredProps.length != 0){
          this.convertToProps();
          this.convertToId();
          let convertNifi = []
          convertNifi.push(this.rawIdNifi);
          convertNifi.push(this.rawDataSetProps);
          this.getPipeline.converter.nifiComponents = convertNifi;
        }
        this.$store.state.registerPipeId = this.generationKey;
        this.$store.state.registerPipeline = this.getPipeline;
        collectorService
          .postPipelineDraft(this.$store.state.registerPipeline)
          .then((res) => {
            this.$store.state.registerPipeline = res;
            this.$store.state.overlay = false;
            this.showDraftCompleted();
          })
          .catch((err) => {
            console.error(err);
          });
        
    },
    showDraftCompleted(){
      let alertPayload = {
            title: "임시저장",
            text:
              "임시저장 성공",
            url: "not Vaild",
          };
          EventBus.$emit("show-alert-popup", alertPayload);
    }
  },
};
</script>
