export default{
    install(Vue){
        Vue.prototype.$checkTimerPattern = function(iv){
            return /^[0-9]+ sec$/g.test(iv);
        }

        Vue.prototype.$removeBlank = function(iv){
            return iv.replace(/^\s+|\s+$/g, '');
        }

        Vue.prototype.$checkAllBlankPattern = function(iv){
            return /[\s]/g.test(iv);
        }
        
        Vue.prototype.$checkBlankPattern = function(iv){
            return /^\s+|\s+$/g.test(iv);
        }
    }
}