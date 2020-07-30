import React from 'react';
import { makeStyles } from '@material-ui/core/styles';
import SearchIcon from '@material-ui/icons/Search';
import {
    TextField,
    Grid,
    Select,
    MenuItem,
    Button,
} from '@material-ui/core';

const useStyles = makeStyles({
    listItem: {
        margin: '10px 0 0 0'
    },
    gamesList: {

    },
    button: {
        // background: '#039BE5',
        color: 'white',
        background: 'linear-gradient(45deg, #FE6B8B 30%, #FF8E53 90%)',
    },
    form: {
        width: '50%',
        margin: '0 auto',
        marginTop: '3%',
    },
    results: {
        maxHeight: 1000,
    },
    textField: {
        width: 400,
    },
    inputSelect: {
        width: 150,
    },
});

const Searchbar = ({
    searchTerm,
    searchType,
    handleTextFieldChange,
    handleSelectInputChange,
    handleSearchSubmit,
}) => {
    const classes = useStyles();

    return (
        <form
            className={classes.form}
            onSubmit={handleSearchSubmit}
            autoComplete="off"
        >
            <Grid container spacing={1} alignItems="flex-end">
                <Grid item>
                    <SearchIcon />
                </Grid>
                <Grid item>
                    <TextField
                        id="games-search"
                        label="Search by title, genre, or year"
                        value={searchTerm}
                        className={classes.textField}
                        onChange={handleTextFieldChange}
                    />
                </Grid>
                <Grid item>
                    <Select
                        id="search-type"
                        value={searchType}
                        onChange={handleSelectInputChange}
                        className={classes.inputSelect}
                    >
                        <MenuItem value="game">Title</MenuItem>
                        <MenuItem value="genre">Genre</MenuItem>
                        <MenuItem value="year">Year</MenuItem>
                        <MenuItem value="genre&year">Genre/Year</MenuItem>
                    </Select>
                </Grid>
                <Grid item>
                    <Button
                        type="submit"
                        variant="contained"
                        className={classes.button}
                    >
                        Go
                    </Button>
                </Grid>
            </Grid>
        </form>
    )
}

export default Searchbar;
