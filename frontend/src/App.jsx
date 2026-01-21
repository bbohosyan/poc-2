import { Container, Typography } from '@mui/material';
import { useEffect, useCallback } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import TableView from './components/TableView.jsx';
import TableForm from './features/table/TableForm.jsx';
import { setRows } from './features/table/tableSlice';
import { fetchRows } from './services/api';

function App() {
  const dispatch = useDispatch();
  const page = useSelector((state) => state.table.page);
  const size = useSelector((state) => state.table.size);

  useEffect(() => {
    console.log("Refetch")
    loadData();
  }, [page]);


  const loadData = useCallback(async () => {
    try {
      const data = await fetchRows(page, size);
      dispatch(setRows(data));
    } catch (error) {
      console.error('Error loading data:', error);
    }
  }, [page, size]) 
  
  // const loadData = async () => {
  //   try {
  //     const data = await fetchRows(page, size);
  //     dispatch(setRows(data));
  //   } catch (error) {
  //     console.error('Error loading data:', error);
  //   }
  // };

  return (
    <Container maxWidth="lg" sx={{ marginTop: 4 }}>
      <Typography variant="h4" gutterBottom>
        Table Management
      </Typography>

    <TableForm onRowAdded={loadData} />
      <TableView />
    </Container>
  );
}

export default App;