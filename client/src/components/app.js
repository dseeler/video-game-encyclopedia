import React, { useState } from 'react';
import GameSearch from './game-search';
import Nav from './nav';
import Plots from './plots';
import WishList from './wish-list';
import './../css/app.css';

const fetchData = (dataExists) => {
    return [ { name: 'game' } ];
};

const wishListClick = () => {
    console.log('test');
};

const App = () => {
    const [activeComponent, setActiveComponent] = useState('games-search');
    const [searchTerm, setSearchTerm] = useState('');
    const [data, setData] = useState([]);
    const [wishList, setWishList] = useState([]);
    const [dataFetched, setDataFetched] = useState(false);

    if (!dataFetched) {
        setData(fetchData);
        setDataFetched(true);
    }

    const handleWishListClick = event => {
        setActiveComponent('wish-list');
    }

    const handleTitleClick = event => {
        setActiveComponent('games-search');
    }

    return (
        <>
            <Nav
                onWishListClick={handleWishListClick}
                onTitleClick={handleTitleClick}
            />
            {
                (activeComponent === 'games-search') ?
                    (
                        <GameSearch />
                    ) : (activeComponent === 'plots') ?
                    (
                        <Plots />
                    ) : (activeComponent === 'wish-list') ?
                    (
                        <WishList />
                    ) : (
                        <>
                        </>
                    )
            }
        </>
    );
};

export default App;
