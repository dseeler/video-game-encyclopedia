import React from 'react';
import GamesList from './games-list';
import { makeStyles } from '@material-ui/core/styles';
import {
    Typography,
} from '@material-ui/core';

const useStyles = makeStyles({
    root: {
        width: '33%',
        margin: '0 auto',
        marginTop: '5%'
    },
});

const WishList = ({
    wishList,
    handleGameClick,
}) => {
    const classes = useStyles();

    return (
        wishList.length === 0 ?
            (
                <Typography className={classes.root} variant="h6">
                    You do not have any items in the wishlist
                </Typography>
            ) : (
                <GamesList
                    data={wishList}
                    handleGameClick={handleGameClick}
                />
            )
    )
}

export default WishList;
