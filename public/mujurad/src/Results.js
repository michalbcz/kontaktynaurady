import React from 'react';
import ResultRow from './ResultRow';

export default class Results extends React.Component {
    render() {
        if (this.props.searchResults === null) {
            // initial state - search was not performed yet
            return null;
        }

        let rows;
        if (this.props.searchResults.length > 0) {
            rows = this.props.searchResults.map((searchResult) => {
                return <ResultRow key={searchResult.name} searchResult={searchResult}  />
            });
        } else {
            // search was performed but with no results
            rows = (
                <tr>
                    <td>Nic jsme nenašli. Zadali jste správně název úřadu?</td>
                </tr>
            );
        }

        return (
            <table className="table">
                <tbody>
                    {rows}
                </tbody>
            </table>
        );
    }
} 