import React from 'react';
import { makeStyles } from '@material-ui/core/styles';
import _ from 'underscore';
import {
    Typography,
    Box,
    Paper,
    List,
    ListItem,
    ListItemText,
    Button,
} from '@material-ui/core';

const useStyles = makeStyles({
    root: {
        margin: '3% 12% 2% 12%'
    },
    center: {
        margin: '0 auto',
    },
    backButton: {
        position: 'relative',
        left: '81%',
    },
});

const GameDetail = ({
    wishList,
    data,
    activeGame,
    handleAddToWishList,
    handleBackClick,
}) => {
    const game = activeGame;
    const classes = useStyles();

    return (
        <Paper elevation={2} className={classes.root}>
            <List>
                <ListItem>
                    <Typography variant="h4">
                        { game.title }
                    </Typography>
                </ListItem>
                <ListItem>
                    <Typography>
                        Metacritic Score: <strong>{game.metacriticScore}</strong>
                    </Typography>
                </ListItem>
                <ListItem>
                    <Typography>
                        Release Date: { game.releaseDate }
                    </Typography>
                </ListItem>
                <ListItem>
                    <Typography>
                        { game.description }
                    </Typography>
                </ListItem>
                <ListItem>
                    <Typography>
                        Genres: {_.uniq(game.genre).map((genre, index, arr) => (
                            (index === arr.length - 1) ?
                                `${genre}` :
                                `${genre}, `
                        ))}
                    </Typography>
                </ListItem>
                <ListItem>
                    <Typography>
                        Platforms: {_.uniq(game.platform).map((platform, index, arr) => (
                            (index === arr.length - 1) ?
                                `${platform}` :
                                `${platform}, `
                        ))}
                    </Typography>
                </ListItem>
                <ListItem>
                    <Typography>
                        Stores: {_.uniq(game.store).map((store, index, arr) => (
                            (index === arr.length - 1) ?
                                `${store}` :
                                `${store}, `
                        ))}
                    </Typography>
                </ListItem>
                <ListItem>
                    <video width="640" height="360" controls>
                        <source src={game.clipLink} type="video/mp4"/>
                    </video>
                </ListItem>
                <ListItem>
                    <Button
                        color="primary"
                        onClick={() => handleAddToWishList(game)}
                    >
                        Add to wishlist!
                    </Button>
                    <Button
                        className={classes.backButton}
                        color="primary"
                        onClick={handleBackClick}
                    >
                        Back
                    </Button>
                </ListItem>
            </List>
        </Paper>
    )
}

export default GameDetail;
