<template>
  <div style="width: 95%">
    <div class="pipelineCreateNavBox mgT20">
      <button class="navBoxBtn" @click="go">
        <div class="numberBox">1</div>
        <div>기본정보 입력</div>
      </button>
      <button class="navBoxBtn">
        <div class="numberBox">2</div>
        <div>데이터 수집</div>
      </button>
      <button class="navBoxBtn">
        <div class="numberBox">3</div>
        데이터 정재
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
    <div class="pipelineListTitle">
      <p class="fsb16">데이터 파이프라인 기본정보 입력</p>
    </div>
  </div>
</template>

<script>
export default {};
</script>
