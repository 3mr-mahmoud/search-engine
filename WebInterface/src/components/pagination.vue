<script>
export default {
  name: 'pagination',
  props: ['collectionData'],
  emits: ['pageChange'],
  methods: {
    pageChange(page) {
      this.$emit('pageChange', page)
    }
  },
}
</script>

<template>
  <nav aria-label="Page navigation example">
    <ul class="flex items-center -space-x-px h-10 text-base">
      <li>
        <a v-if="collectionData.currentPage > 1" @click.prevent="pageChange(collectionData.currentPage - 1)"
          class="flex items-center justify-center px-4 h-10 ms-0 leading-tight text-gray-500 bg-white border border-e-0 border-gray-300 rounded-s-lg hover:bg-gray-100 hover:text-gray-700 dark:bg-gray-800 dark:border-gray-700 dark:text-gray-400 dark:hover:bg-gray-700 dark:hover:text-white">
          <svg class="w-3.5 h-3.5 me-2 rtl:rotate-180" aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="none"
            viewBox="0 0 14 10">
            <path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M13 5H1m0 0 4 4M1 5l4-4" />
          </svg>
          Previous
        </a>
      </li>
      <li v-for="(page, index) in collectionData.pagesAvailable" :key="index">
        <a href="#" @click.prevent="pageChange(page)" :class="{
          'z-10 text-red-600 border-red-300 bg-red-50 hover:bg-red-100 hover:text-red-700 dark:border-gray-700 dark:bg-gray-700 dark:text-white': page == collectionData.currentPage,
          'text-gray-500 bg-white border-gray-300 hover:bg-gray-100 hover:text-gray-700 dark:bg-gray-800 dark:border-gray-700 dark:text-gray-400 dark:hover:bg-gray-700 dark:hover:text-white': page != collectionData.currentPage
        }" class="flex items-center justify-center px-4 h-10 leading-tight  border">{{
          page }}</a>
      </li>
      <li>
        <a href="#" v-if="collectionData.currentPage < collectionData.pagesAvailable"
          @click.prevent="pageChange(collectionData.currentPage + 1)"
          class="flex items-center justify-center px-4 h-10 leading-tight text-gray-500 bg-white border border-gray-300 rounded-e-lg hover:bg-gray-100 hover:text-gray-700 dark:bg-gray-800 dark:border-gray-700 dark:text-gray-400 dark:hover:bg-gray-700 dark:hover:text-white">
          Next
          <svg class="w-3.5 h-3.5 ms-2 rtl:rotate-180" aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="none"
            viewBox="0 0 14 10">
            <path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M1 5h12m0 0L9 1m4 4L9 9" />
          </svg>
        </a>
      </li>
    </ul>
  </nav>
</template>
