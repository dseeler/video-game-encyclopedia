import React from 'react';
import Box from '@material-ui/core/Box';
import CircularProgress from '@material-ui/core/CircularProgress';

const GameSearch = (data) => {
    return (data.length === 0) ?
        (
            <Box
                height="25%"
                display="flex"
                alignItems="center"
                justifyContent="center"
            >
                <CircularProgress />
            </Box>
        ) : (
            <>
                game-search
            </>
        );
}

export default GameSearch;
