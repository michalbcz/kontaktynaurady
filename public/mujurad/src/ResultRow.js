import React from 'react';

export default class ResultRow extends React.Component {
    render() {
        return (
            <tr>
                <td>{this.props.searchResult.name}</td>
                <td>[google places]</td>
                <td>[word cloud]</td>
                <td>[hejtmail]</td>
            </tr>
        );
    }
}