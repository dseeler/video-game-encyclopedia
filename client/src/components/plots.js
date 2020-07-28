import React from 'react';
// import {
//     BarChart,
//     Bar,
//     XAxis,
//     YAxis,
//     CartesianGrid,
//     Tooltip,
//     LineChart,
//     Line,
// } from 'recharts';

// const shapeDataForBarChart = (games, sortBy, error = false) =>
//     games.reduce((acc, curr) => {
//         const value = error ? curr.error[sortBy] : curr[sortBy];
//         return acc.find((obj: ChartData) => obj[sortBy] === value)
//             ? acc.map((obj: ChartData) => ({
//                   [sortBy]: obj[sortBy],
//                   errors: obj[sortBy] === value ? obj.errors + 1 : obj.errors,
//               }))
//             : acc.concat({
//                   [sortBy]: value,
//                   errors: 1,
//               });
//     }, []);

const Plots = (data) => (
    <>
    </>
);

export default Plots;
