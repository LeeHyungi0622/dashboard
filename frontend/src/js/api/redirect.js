import axios from "axios";

class redirect {
    getNiFiURL() {
        return axios
            .get(`/redirectNiFiUrl`)
            .then((response) => {
                return response.data;
            })
            .catch((error) => error);
    }
}

export default new redirect();