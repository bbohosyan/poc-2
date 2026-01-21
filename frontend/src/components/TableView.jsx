import React from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, Pagination, Box, IconButton } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import { useSnackbar } from 'notistack';
import { setPage, setRows } from '../features/table/tableSlice';
import { deleteRowById, fetchRows } from '../services/api';

function TableView() {
  const dispatch = useDispatch();
  const { enqueueSnackbar } = useSnackbar();
  const rows = useSelector((state) => state.table.rows);
  const totalCount = useSelector((state) => state.table.totalCount);
  const page = useSelector((state) => state.table.page);
  const size = useSelector((state) => state.table.size);

  const totalPages = Math.ceil(totalCount / size);

  const handlePageChange = (_, value) => {
    dispatch(setPage(value - 1));
  };

  const handleDelete = async (id) => {
    try {
      await deleteRowById(id);
      enqueueSnackbar('Row deleted successfully!', { variant: 'success' });
      
      const data = await fetchRows(page, size);
      dispatch(setRows(data));
    } catch (error) {
      console.error('Error deleting row:', error);
      enqueueSnackbar('Failed to delete row', { variant: 'error' });
    }
  };

  return (
    <>
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell sx={{ width: '80px' }}>ID</TableCell>
              <TableCell sx={{ width: '120px' }}>Type Number</TableCell>
              <TableCell sx={{ width: '120px' }}>Type Selector</TableCell>
              <TableCell>Type Free Text</TableCell>
              <TableCell sx={{ width: '100px' }}>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {rows.map((row) => (
              <TableRow key={row.id}>
                <TableCell>{row.id}</TableCell>
                <TableCell>{row.typeNumber}</TableCell>
                <TableCell>{row.typeSelector}</TableCell>
                <TableCell 
                  sx={{ 
                    maxWidth: '400px',
                    overflow: 'hidden',
                    textOverflow: 'ellipsis',
                    whiteSpace: 'nowrap'
                  }}
                >
                  {row.typeFreeText}
                </TableCell>
                <TableCell>
                  <IconButton 
                    color="error" 
                    onClick={() => handleDelete(row.id)}
                    aria-label="delete"
                  >
                    <DeleteIcon />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
      
      <Box sx={{ display: 'flex', justifyContent: 'center', marginTop: 3 }}>
        <Pagination 
          count={totalPages} 
          page={page + 1} 
          onChange={handlePageChange}
          color="primary"
        />
      </Box>
    </>
  );
}

export default TableView;