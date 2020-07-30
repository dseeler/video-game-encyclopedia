import React, { useState, useEffect } from 'react';
import Searchbar from './searchbar';
import Nav from './nav';
import Plots from './plots';
import WishList from './wish-list';
import GameDetail from './game-detail';
import GamesList from './games-list';
import { makeStyles } from '@material-ui/core/styles';
import './../css/app.css';
import {
    Box,
    CircularProgress,
    Typography,
} from '@material-ui/core';

const formatSearch = (search) => (
    search.replace(/ /g, '_').toLowerCase()
);

const useStyles = makeStyles({
    fetchingCircle: {
        margin: '10% 0% 0% 0%'
    },
    center: {
        width: '14%',
        margin: '0 auto',
        marginTop: '5%',
    },
});

const App = () => {
    const [activeComponent, setActiveComponent] = useState('games-search');
    const [searchTerm, setSearchTerm] = useState('');
    const [searchType, setSearchType] = useState('game');
    const [data, setData] = useState([]);
    const [wishList, setWishList] = useState([]);
    const [fetching, setFetching] = useState(false);
    const [hasError, setHasError] = useState(false);
    const [newSearch, setNewSearch] = useState(false);
    const [activeGame, setActiveGame] = useState();

    const handleTextFieldChange = event => setSearchTerm(event.target.value);
    const handleSelectInputChange = event => setSearchType(event.target.value);
    const handleWishListClick = event => setActiveComponent('wish-list');
    const handleTitleClick = event => setActiveComponent('games-list');
    const handleGameClick = game => {
        setActiveGame(game);
        setActiveComponent('game-detail');
    }
    const handleAddToWishList = game => {
        setWishList(wishList.concat(game));
    };
    const handleSearchSubmit = event => {
        event.preventDefault();
        setActiveComponent('games-list');
        setNewSearch(true);
    };
    const handleBackClick = () => setActiveComponent('games-list');

    useEffect(() => {
        setHasError(false);
        setNewSearch(false);
        if (searchTerm !== '') {
            setFetching(true);
            fetch(`http://localhost:5000/${searchType}/${formatSearch(searchTerm)}`)
                .then(res => res.json())
                .then(json => {
                    setData(json);
                    setFetching(false);
                })
                .catch(error => {
                    setFetching(false);
                    setHasError(true);
                });
        }
        setSearchTerm('');
    }, [newSearch]);

    const classes = useStyles();

    return (
        <>
            <Nav
                handleWishListClick={handleWishListClick}
                handleTitleClick={handleTitleClick}
            />
            <Searchbar
                searchTerm={searchTerm}
                searchType={searchType}
                handleTextFieldChange={handleTextFieldChange}
                handleSelectInputChange={handleSelectInputChange}
                handleSearchSubmit={handleSearchSubmit}
            />
            {
                (activeComponent === 'games-list') ?
                    (
                        fetching ?
                            (
                                <Box
                                    className={classes.fetchingCircle}
                                    height="100%"
                                    display="flex"
                                    alignItems="center"
                                    justifyContent="center"
                                >
                                    <CircularProgress />
                                </Box>
                            ) : hasError ? (
                                <Typography
                                    className={classes.center}
                                    variant="h6"
                                >
                                    No results found
                                </Typography>
                            ) : (
                                <GamesList
                                    data={data}
                                    handleGameClick={handleGameClick}
                                />
                            )
                    ) : (activeComponent === 'plots') ?
                    (
                        <Plots />
                    ) : (activeComponent === 'wish-list') ?
                    (
                        <WishList
                            wishList={wishList}
                            handleGameClick={handleGameClick}
                        />
                    ) : (activeComponent === 'game-detail') ?
                    (
                        <GameDetail
                            data={data}
                            wishList={wishList}
                            activeGame={activeGame}
                            handleAddToWishList={handleAddToWishList}
                            handleBackClick={handleBackClick}
                        />
                    ) : (
                        <>
                        </>
                    )
            }
        </>
    );
};

export default App;
