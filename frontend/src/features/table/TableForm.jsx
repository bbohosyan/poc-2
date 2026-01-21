import React, { useState } from 'react';
import { useDispatch } from 'react-redux';
import { TextField, Button, Select, MenuItem, FormControl, InputLabel, Box, FormHelperText } from '@mui/material';
import { useSnackbar } from 'notistack';
import { addRow } from './tableSlice';
import { createRow } from '../../services/api';


function TableForm({ onRowAdded }) {
  const [typeNumber, setTypeNumber] = useState('');
  const [typeSelector, setTypeSelector] = useState('');
  const [typeFreeText, setTypeFreeText] = useState('');
  const [errors, setErrors] = useState({});
  
  const dispatch = useDispatch();
  const { enqueueSnackbar } = useSnackbar();

  const validateForm = () => {
    const newErrors = {};
    
    if (!typeNumber || typeNumber === '') {
      newErrors.typeNumber = 'Type Number is required';
    } else if (parseInt(typeNumber) < 1) {
      newErrors.typeNumber = 'Type Number must be at least 1';
    } else if (parseInt(typeNumber) > 2147483647) {
      newErrors.typeNumber = 'Type Number must not exceed 2,147,483,647';
    }
    
    if (!typeSelector || typeSelector === '') {
      newErrors.typeSelector = 'Type Selector is required';
    }
    
    if (!typeFreeText || typeFreeText.trim() === '') {
      newErrors.typeFreeText = 'Type Free Text is required';
    } else if (typeFreeText.length > 1000) {
      newErrors.typeFreeText = 'Type Free Text must not exceed 1000 characters';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async () => {
    if (!validateForm()) {
      enqueueSnackbar('Please fix validation errors', { variant: 'error' });
      return;
    }

    const rowData = {
      typeNumber: parseInt(typeNumber),
      typeSelector,
      typeFreeText,
    };

    try {
      const newRow = await createRow(rowData);

      setErrors({});

      enqueueSnackbar('Row added successfully!', { variant: 'success' });

      if (onRowAdded) onRowAdded();
    } catch (error) {
      console.error('Error creating row:', error);
      
      let errorMessage = 'Failed to add row';
      
      if (error.response?.data?.violations) {
        const violations = error.response.data.violations;
        errorMessage = violations.map(v => v.message).join(', ');
      } else if (error.response?.data?.message) {
        errorMessage = error.response.data.message;
      } else if (error.response?.data?.error) {
        errorMessage = error.response.data.error;
      } else if (error.message) {
        errorMessage = error.message;
      }
      
      enqueueSnackbar(errorMessage, { variant: 'error' });
    }
  };

  return (
    <Box sx={{ display: 'flex', gap: 2, marginBottom: 3, alignItems: 'flex-start' }}>
      <TextField
        type="number"
        label="Type Number"
        value={typeNumber}
        onChange={(e) => {
          setTypeNumber(e.target.value);
          if (errors.typeNumber) setErrors({...errors, typeNumber: null});
        }}
        error={!!errors.typeNumber}
        helperText={errors.typeNumber || ' '}
        inputProps={{ max: 2147483647, min: 1 }}
        sx={{ width: 180 }}
      />
      
      <FormControl sx={{ width: 180 }} error={!!errors.typeSelector}>
        <InputLabel id="type-selector-label">Type Selector</InputLabel>
        <Select
          labelId="type-selector-label"
          value={typeSelector}
          label="Type Selector"
          onChange={(e) => {
            setTypeSelector(e.target.value);
            if (errors.typeSelector) setErrors({...errors, typeSelector: null});
          }}
        >
          <MenuItem value="A">Option A</MenuItem>
          <MenuItem value="B">Option B</MenuItem>
          <MenuItem value="C">Option C</MenuItem>
        </Select>
        <FormHelperText>{errors.typeSelector || ' '}</FormHelperText>
      </FormControl>
      
      <TextField
        label="Type Free Text"
        value={typeFreeText}
        onChange={(e) => {
          setTypeFreeText(e.target.value);
          if (errors.typeFreeText) setErrors({...errors, typeFreeText: null});
        }}
        error={!!errors.typeFreeText}
        helperText={errors.typeFreeText || `${typeFreeText.length}/1000`}
        inputProps={{ maxLength: 1000 }}
        sx={{ flexGrow: 1 }}
      />
      
      <Button 
        variant="contained" 
        onClick={handleSubmit}
        sx={{ height: '56px', marginTop: 0 }}
      >
        Add Row
      </Button>
    </Box>
  );
}

export default TableForm;