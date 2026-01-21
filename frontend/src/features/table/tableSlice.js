import { createSlice } from '@reduxjs/toolkit';

const initialState = {
  rows: [],
  totalCount: 0,
  page: 0,
  size: 10,
};

const tableSlice = createSlice({
  name: 'table',
  initialState,
  reducers: {
    addRow: (state, action) => {
      state.rows.push(action.payload);
      state.totalCount += 1;
    },
    setRows: (state, action) => {
      state.rows = action.payload.data;
      state.totalCount = action.payload.totalCount;
      state.page = action.payload.page;
      state.size = action.payload.size;
    },
    setPage: (state, action) => {
      state.page = action.payload;
    },
    deleteRow: (state, action) => {
      state.rows = state.rows.filter(row => row.id !== action.payload);
      state.totalCount -= 1;
    },
  },
});

export const { addRow, setRows, setPage, deleteRow } = tableSlice.actions;
export default tableSlice.reducer;