<script>
import apiClient from './apiClient';
import pagination from './components/pagination.vue';
import { watch } from 'vue';
export default {
  components: { pagination },
  data() {
    return {
      loading: false,
      isDark: false,
      haveResults: false,
      searchFocused: false,
      lastSearch: '',
      search: '',
      onSuggestions: false,
      showSuggestions: false,
      page: 1,
      resultsCollection: {
        results: [],
      },
      suggestions: [
      ],
    };
  },
  mounted() {
    this.page = this.$route.query.page ? parseInt(this.$route.query.page) : 1;
    this.applyTheme();
  },
  methods: {
    searchSuggestion(suggestion) {
      this.search = suggestion;
      this.page = 1;
      this.onSuggestions = false;
      this.searchFocused = false;
      this.goToSearchLink();
    },
    reset() {
      this.search = '';
      this.page = 1;
      this.haveResults = false;
      this.$router.push({ query: {} });
    },
    handlePageChange(page) {
      this.page = page;
      this.$router.push({ query: { ...this.$route.query, page: this.page } });
    },
    goToSearchLink() {
      this.$router.push({ query: { ...this.$route.query, search: this.search, page: 1 } });
    },
    searchHandler() {
      this.loading = true;
      apiClient.get('/search', {
        params: {
          keyword: this.search,
          page: this.page
        }
      }).then(response => {
        this.loading = false;
        this.resultsCollection = response.data;
        this.haveResults = true;
      }).catch(error => {
        console.error('Error searching', error);
      });
    },
    getSuggestions() {
      apiClient.get('/suggestions', {
        params: {
          keyword: this.search
        }
      }).then(response => {
        this.suggestions = response.data;
      }).catch(error => {
        console.error('Error searching', error);
      });
    },
    toggleTheme() {
      this.isDark = !this.isDark;
      this.applyTheme();
    },
    removeSearchFocus() {
      if (!this.onSuggestions) {
        this.searchFocused = false;
      }
    },
    applyTheme() {
      const element = document.querySelector('html')
      if (this.isDark) {
        element.classList.add('dark')
      } else {
        element.classList.remove('dark')
      }
    }

  },
  watch: {
    search(newVal) {
      if (newVal) {
        this.getSuggestions();
      }
    },
    '$route.query': {
      immediate: true,
      handler: function (val) {
        if (val.search) {
          this.search = val.search;
          if (val.page) {
            this.page = val.page;
          } else {
            this.page = 1;
          }
          this.searchHandler();
        } else {
          this.search = val.search;
          this.haveResults = false;
        }
      }
    },
  }
}
</script>

<template>
  <div class="h-full dark:bg-zinc-900">
    <div class="flex transition-all duration-500   bg-gray-50 dark:bg-zinc-800 items-center relative w-full"
      :class="{ 'h-full justify-center': !haveResults, 'h-40 justify-start': haveResults }">

      <button id="theme-toggle" type="button" @click="toggleTheme" aria-label="Toggle Dark Mode"
        class="text-gray-500 dark:text-gray-400 hover:bg-gray-100 absolute top-5 right-5 dark:hover:bg-gray-700 focus:outline-none focus:ring-4 focus:ring-gray-200 dark:focus:ring-gray-700 rounded-lg text-sm p-2.5">
        <svg id="theme-toggle-dark-icon" v-if="!isDark" class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20"
          xmlns="http://www.w3.org/2000/svg">
          <path d="M17.293 13.293A8 8 0 016.707 2.707a8.001 8.001 0 1010.586 10.586z"></path>
        </svg>
        <svg id="theme-toggle-light-icon" v-else class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20"
          xmlns="http://www.w3.org/2000/svg">
          <path
            d="M10 2a1 1 0 011 1v1a1 1 0 11-2 0V3a1 1 0 011-1zm4 8a4 4 0 11-8 0 4 4 0 018 0zm-.464 4.95l.707.707a1 1 0 001.414-1.414l-.707-.707a1 1 0 00-1.414 1.414zm2.12-10.607a1 1 0 010 1.414l-.706.707a1 1 0 11-1.414-1.414l.707-.707a1 1 0 011.414 0zM17 11a1 1 0 100-2h-1a1 1 0 100 2h1zm-7 4a1 1 0 011 1v1a1 1 0 11-2 0v-1a1 1 0 011-1zM5.05 6.464A1 1 0 106.465 5.05l-.708-.707a1 1 0 00-1.414 1.414l.707.707zm1.414 8.486l-.707.707a1 1 0 01-1.414-1.414l.707-.707a1 1 0 011.414 1.414zM4 11a1 1 0 100-2H3a1 1 0 000 2h1z"
            fill-rule="evenodd" clip-rule="evenodd"></path>
        </svg>
      </button>

      <div class="search-bar-container flex justify-center transition-all delay-150 duration-500"
        :class="{ 'flex-col max-w-xl w-3/5': !haveResults, 'w-4/5 items-center gap-4': haveResults }">

        <div class="logo-container transition-all  mb-1 relative" :class="{ 'hidden': haveResults }">
          <img src="./assets/logo_back.png" class="logo-back absolute "
            :class="{ 'focused': searchFocused, 'rotating': loading }" />
          <img alt="Search Engine logo " src="./assets/logo1.png" class="logo-front absolute" />
        </div>


        <h3 class="text-3xl text-center title" @click="reset"
          :class="{ 'mb-5 block ': !haveResults, 'animate__animated animate__rubberBand animate__delay-2s  cursor-pointer': haveResults }">
          <b class="text-red-1000 mr-1">Dragon</b>
          <b class="text-yellow-1000">Ball</b>
        </h3>

        <div class="flex" :class="{ 'w-2/3': haveResults }">
          <div class="relative w-full">
            <div class="absolute inset-y-0 start-0 flex items-center ps-3 pointer-events-none">
              <svg class="w-4 h-4 text-red-500 dark:text-red-600" xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 448 512" fill="currentColor">
                <path
                  d="M323.6 51.2c-20.8 19.3-39.6 39.6-56.2 60C240.1 73.6 206.3 35.5 168 0 69.7 91.2 0 210 0 281.6 0 408.9 100.3 512 224 512s224-103.2 224-230.4c0-53.3-52-163.1-124.4-230.4zm-19.5 340.7C282.4 407 255.7 416 226.9 416 154.7 416 96 368.3 96 290.8c0-38.6 24.3-72.6 72.8-130.8 6.9 8 98.8 125.3 98.8 125.3l58.6-66.9c4.1 6.9 7.9 13.6 11.3 20 27.4 52.2 15.8 119-33.4 153.4z" />
              </svg>
            </div>
            <form @submit.prevent="goToSearchLink">
              <input type="text" id="voice-search" v-model="search"
                class="bg-white border border-gray-300 focus:outline-none text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-gray-500 block w-full ps-10 p-2.5  dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-gray-500 dark:focus:border-gray-500"
                placeholder="Search Games, Coding..." @focus="searchFocused = true" @blur="removeSearchFocus"
                autocomplete="off" />
              <div class="suggestions absolute z-20 w-full" @mouseover="onSuggestions = true"
                @mouseout="onSuggestions = false" v-if="suggestions.length > 0 && searchFocused && search !== ''">
                <ul
                  class="absolute w-full bg-white dark:bg-gray-700 dark:border-gray-800 dark:text-white  border border-gray-300 rounded-lg mt-1">
                  <template v-for="(suggestion, index) in suggestions" :key="index">
                    <li v-if="index <= 5"
                      class="p-2.5 hover:bg-gray-50 dark:hover:bg-gray-600 rounded-lg cursor-pointer"
                      @click="searchSuggestion(suggestion.keyword)">{{
                        suggestion.keyword
                      }}</li>
                  </template>
                </ul>
              </div>
              <button v-if="haveResults" type="button" @click="reset"
                class="absolute inset-y-0 end-0 flex items-center pe-3">
                <svg aria-hidden="true" xmlns="http://www.w3.org/2000/svg"
                  class="w-4 h-4 text-gray-500 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white"
                  viewBox="0 0 352 512" fill="currentColor">
                  <path
                    d="M242.7 256l100.1-100.1c12.3-12.3 12.3-32.2 0-44.5l-22.2-22.2c-12.3-12.3-32.2-12.3-44.5 0L176 189.3 75.9 89.2c-12.3-12.3-32.2-12.3-44.5 0L9.2 111.5c-12.3 12.3-12.3 32.2 0 44.5L109.3 256 9.2 356.1c-12.3 12.3-12.3 32.2 0 44.5l22.2 22.2c12.3 12.3 32.2 12.3 44.5 0L176 322.7l100.1 100.1c12.3 12.3 32.2 12.3 44.5 0l22.2-22.2c12.3-12.3 12.3-32.2 0-44.5L242.7 256z" />
                </svg>
              </button>
            </form>
          </div>
          <button type="submit" @click="goToSearchLink"
            class="inline-flex items-center py-2.5 px-3 ms-2 text-sm font-medium text-white bg-yellow-900 rounded-lg border border-yellow-900 hover:bg-yellow-1000 focus:ring-4 focus:outline-none focus:ring-yellow-300 dark:bg-yellow-1000 dark:hover:bg-yellow-700 dark:focus:ring-yellow-800">
            <svg class="w-4 h-4 me-2" aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="none"
              viewBox="0 0 20 20">
              <path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="m19 19-4-4m0-7A7 7 0 1 1 1 8a7 7 0 0 1 14 0Z" />
            </svg>Search
          </button>
        </div>


      </div>

      <div class="border-bottom absolute bottom-0 h-1 " :class="{ 'h-1': haveResults, 'hidden': !haveResults }">
      </div>
    </div>

    <div class="max-w-6xl mx-auto px-10" v-if="haveResults && resultsCollection.results.length > 0">
      <div class="flex flex-col gap-4 mt-4">
        <h2 class="text-base font-light text-gray-600 dark:text-gray-100">About {{ resultsCollection.total }}
          Results ({{ parseFloat(resultsCollection.elapsedTime).toFixed(3) }} seconds) </h2>
        <div class="flex flex-col gap-2" v-for="(result, index) in resultsCollection.results" :key="index">
          <a class="hover:underline font-bold text-yellow-1000 visited:text-red-1000 text-2xl" :href="result.url">{{
            result.title
          }}</a>
          <p class="text-gray-600 dark:text-gray-300"
            v-html="result.statements.length > 0 ? result.statements[0] : result.description">
          </p>
        </div>

        <div class="flex w-100 justify-center">
          <pagination class="mt-8 mb-16" :collectionData="resultsCollection" @pageChange="handlePageChange" />
        </div>
      </div>
    </div>

    <div class="max-w-6xl mx-auto px-10" v-if="haveResults && resultsCollection.results.length == 0">
      <div class="flex flex-col gap-4 mt-6">
        <h2 class="text-2xl font-light text-gray-600 dark:text-gray-100">No Results Found </h2>
      </div>
    </div>

  </div>
</template>

<style scoped>
.title {
  --animate-delay: 0.25s;
}

.logo-front {
  width: 90%;
  left: 25px;
  top: 25px;
}

.logo-back {
  transition: 0.3s all;
}

.border-bottom {
  width: 100%;
  background-repeat: no-repeat;
  background-size: cover;
  background-image: url('./assets/border-bottom.png');
}

.logo-back.focused {
  transform: rotate(90deg);
}

.logo-container {
  height: 13rem;
  align-self: center;
  padding: 1em;
  width: 12rem;
}

.title-first-word {
  color: #ff0322;
  margin-right: 0.2em;
}

.title-second-word {
  color: #fdcb00;
}


@-webkit-keyframes rotating

/* Safari and Chrome */
  {
  from {
    -webkit-transform: rotate(0deg);
    -o-transform: rotate(0deg);
    transform: rotate(0deg);
  }

  to {
    -webkit-transform: rotate(360deg);
    -o-transform: rotate(360deg);
    transform: rotate(360deg);
  }
}

@keyframes rotating {
  from {
    -ms-transform: rotate(0deg);
    -moz-transform: rotate(0deg);
    -webkit-transform: rotate(0deg);
    -o-transform: rotate(0deg);
    transform: rotate(0deg);
  }

  to {
    -ms-transform: rotate(360deg);
    -moz-transform: rotate(360deg);
    -webkit-transform: rotate(360deg);
    -o-transform: rotate(360deg);
    transform: rotate(360deg);
  }
}

.rotating {
  -webkit-animation: rotating 2s linear infinite;
  -moz-animation: rotating 2s linear infinite;
  -ms-animation: rotating 2s linear infinite;
  -o-animation: rotating 2s linear infinite;
  animation: rotating 2s linear infinite;
}
</style>
