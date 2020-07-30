import React from 'react';
import { makeStyles } from '@material-ui/core/styles';
import AppBar from '@material-ui/core/AppBar';
import Typography from '@material-ui/core/Typography';
import Toolbar from '@material-ui/core/Toolbar';
import Button from '@material-ui/core/Button';

const useStyles = makeStyles({
    toolbarButtons: {
        marginLeft: 'auto',
        color: 'inherit',
    },
    root: {
        background: 'linear-gradient(45deg, #FE6B8B 30%, #FF8E53 90%)',
    },
    title: {
        cursor: 'pointer',
    },
});

const Nav = ({
    handleWishListClick,
    handleTitleClick,
}) => {
    const classes = useStyles();

    return (
        <div>
            <AppBar position="static" className={classes.root}>
                <Toolbar>
                    <Typography
                        className={classes.title}
                        variant="h2"
                        onClick={handleTitleClick}
                    >
                        Video Game Encyclopedia
                    </Typography>
                    <Button
                        className={classes.toolbarButtons}
                        onClick={handleWishListClick}
                    >
                        Wish List
                    </Button>
                </Toolbar>
            </AppBar>
        </div>
    );
}

export default Nav;
