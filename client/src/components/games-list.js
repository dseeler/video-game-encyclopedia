import React from 'react';
import { makeStyles } from '@material-ui/core/styles';
import {
    List,
    ListItem,
    ListItemText,
} from '@material-ui/core';

const useStyles = makeStyles({
    listItem: {
        margin: '20px 0px 20px 0px',
        boxShadow: '0 1px 3px rgba(0,0,0,0.12), 0 1px 2px rgba(0,0,0,0.24)',
        transition: 'all 0.3s cubic-bezier(.25,.8,.25,1)',
        "&:hover": {
            cursor: 'pointer',
            boxShadow: '0 3px 6px rgba(0,0,0,0.16), 0 3px 6px rgba(0,0,0,0.23)',
        },
    },
    list: {
        margin: '0 3% 0 3%'
    },
    listItemText: {
        margin: '0 0 0 20px'
    }
});

const GamesList = ({
    data,
    handleGameClick,
}) => {
    const classes = useStyles();
    return (
        <List className={classes.list}>
            {
                data.map(game => (
                    <ListItem className={classes.listItem} onClick={() => handleGameClick(game)}>
                        <img alt={game.title} src={game.imageLink} width="160" height="90" />
                        <ListItemText
                            className={classes.listItemText}
                            primary={game.title}
                            secondary={game.description}
                        />
                    </ListItem>
                ))
            }
        </List>
    )
};

export default GamesList;
