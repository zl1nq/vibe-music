import { defineStore } from 'pinia'
import type { ResultTable } from '@/api/system'

export const useLibraryStore = defineStore('LibraryStore', {
  state: () => ({
    tableData: null as ResultTable['data'] | null,
  }),
  actions: {
    setTableData(data: ResultTable['data']) {
      this.tableData = data
    },
  },
}) 