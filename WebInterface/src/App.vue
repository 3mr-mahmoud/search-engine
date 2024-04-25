<script>
export default {
  data() {
    return {
      isDark: false,
      searchFocused: false,
    };
  },
  mounted() {
    this.applyTheme();
  },
  methods: {
    toggleTheme() {
      this.isDark = !this.isDark;
      this.applyTheme();
    },
    applyTheme() {
      const element = document.querySelector('html')
      if (this.isDark) {
        element.classList.add('dark')
      } else {
        element.classList.remove('dark')
      }
    }

  }
}
</script>

<template>
  <div class="flex justify-center bg-gray-50 dark:bg-zinc-800 items-center relative w-full h-full">

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

    <div class="search-bar-container w-4/5 max-w-xl flex flex-col justify-center mx-auto">
      <div class="logo-container mb-1 relative">
        <img src="./assets/logo_back.png" class="logo-back absolute" :class="{ 'focused': searchFocused }" />
        <img alt="Search Engine logo " src="./assets/logo1.png" class="logo-front absolute" />
      </div>


      <h3 class="text-3xl mb-5 text-center">
        <b class="text-red-1000 mr-1">Dragon</b>
        <b class="text-yellow-1000">Ball</b>
      </h3>

      <div class="flex">
        <div class="relative w-full">
          <div class="absolute inset-y-0 start-0 flex items-center ps-3 pointer-events-none">
            <svg class="w-4 h-4 text-red-500 dark:text-red-600" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 448 512"
              fill="currentColor">
              <path
                d="M323.6 51.2c-20.8 19.3-39.6 39.6-56.2 60C240.1 73.6 206.3 35.5 168 0 69.7 91.2 0 210 0 281.6 0 408.9 100.3 512 224 512s224-103.2 224-230.4c0-53.3-52-163.1-124.4-230.4zm-19.5 340.7C282.4 407 255.7 416 226.9 416 154.7 416 96 368.3 96 290.8c0-38.6 24.3-72.6 72.8-130.8 6.9 8 98.8 125.3 98.8 125.3l58.6-66.9c4.1 6.9 7.9 13.6 11.3 20 27.4 52.2 15.8 119-33.4 153.4z" />
            </svg>
          </div>
          <input type="text" id="voice-search"
            class="bg-gray-50 border border-gray-300 focus:outline-none text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-gray-500 block w-full ps-10 p-2.5  dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-gray-500 dark:focus:border-gray-500"
            placeholder="Search Games, Coding..." @focus="searchFocused = true" @blur="searchFocused = false" />
          <button type="button" class="absolute inset-y-0 end-0 flex items-center pe-3">
            <svg class="w-4 h-4 text-gray-500 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white"
              aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 16 20">
              <path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M15 7v3a5.006 5.006 0 0 1-5 5H6a5.006 5.006 0 0 1-5-5V7m7 9v3m-3 0h6M7 1h2a3 3 0 0 1 3 3v5a3 3 0 0 1-3 3H7a3 3 0 0 1-3-3V4a3 3 0 0 1 3-3Z" />
            </svg>
          </button>
        </div>
        <button type="submit"
          class="inline-flex items-center py-2.5 px-3 ms-2 text-sm font-medium text-white bg-yellow-900 rounded-lg border border-yellow-900 hover:bg-yellow-1000 focus:ring-4 focus:outline-none focus:ring-yellow-300 dark:bg-yellow-1000 dark:hover:bg-yellow-700 dark:focus:ring-yellow-800">
          <svg class="w-4 h-4 me-2" aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="none"
            viewBox="0 0 20 20">
            <path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="m19 19-4-4m0-7A7 7 0 1 1 1 8a7 7 0 0 1 14 0Z" />
          </svg>Search
        </button>
      </div>


    </div>

  </div>
</template>

<style scoped>
.logo-front {
  width: 90%;
  left: 25px;
  top: 25px;
}

.logo-back {
  transition: 0.3s all;
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
</style>
