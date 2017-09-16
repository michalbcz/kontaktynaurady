import React from 'react';
import axios from 'axios';
import GooglePlacesReview from './GooglePlacesReview'
import { TagCloud } from "react-tagcloud";

const PLACES_API_KEY = 'AIzaSyBzLHOdWHy_0eY4j3SYYbEIKU5OtmGOo2U'

export default class ResultRow extends React.Component {

    constructor(props) {
        super(props);
        this.state = { 
            reviewScore: null,    
            reviews: [],
            googleMapsUrl: null,
            mailToText: this.createMailToText(this.props.searchResult.email)        
        }

    }

    createMailToText(email) {
        let body = `
        Dobrý den,

           děkuji za skvělou práci, kterou odvádíte.  

        `;
        return encodeURI(`mailto:${email}?subject=Poděkování&body=${body}`)
    }

    componentDidMount() {
        const self = this;
        
        axios.get('https://cors.now.sh/https://maps.googleapis.com/maps/api/place/textsearch/json', {
            params: {
                key: PLACES_API_KEY,
                query: 'urad near ' + this.props.searchResult.name,              
                location: `${this.props.searchResult.latitude}, ${this.props.searchResult.longitude}`,
                radius: 100
            }
        }).then((result) => {
            console.log('Places textsearch', this.props.searchResult.name, result)

            const placeWithReview = result.data.results.find((place) => place.rating);

            if (placeWithReview) {
                const placeId = placeWithReview.place_id;
                
                axios.get('https://cors.now.sh/https://maps.googleapis.com/maps/api/place/details/json', {
                    params: {
                        key: PLACES_API_KEY,
                        placeid: placeId                        
                    }
                }).then((result) => {
                    console.log('Places detail ', placeWithReview.name, result)
                    const placeDetail = result.data.result
                    const rating = placeDetail.rating;
                    const reviews = placeDetail.reviews;
                    const googleMapsUrl = placeDetail.url;
                    
                    self.setState({ 
                        reviewScore: rating,
                        reviews: reviews, 
                        googleMapsUrl: googleMapsUrl 
                    });
                        
                }).catch((error) => {
                    console.log(`Error when fetching place detail (id: ${placeId} ): `, error);
                })
            } else {
                self.setState({ reviewScore: 0 })
            }
            
        }).catch((error) => {
            console.log('Error when fetching nearby places: ', error);
        })
    }

    getData() {
        return this.count(this.flatten(this.state.reviews.map((review) => {
            let text = review.text;
            text = text.replace(/[.,?!\(\)\[\]\-=]/, ""); // remove all word/sentences characters
            text = text.toLowerCase();
            return text.split(/\s/).filter((t) => t.trim() != "")
        }))).filter((tuple) => tuple.count >= 1);
    }

    flatten(list) { 
         return list.reduce((a, b) => a.concat(Array.isArray(b) ? this.flatten(b) : b), [])
    };

    count(names) {
        // Count occurrences using a map (https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Map)
        let map = new Map();
        for (let name of names) {
            map.set(name, (map.get(name) || 0) + 1);
        }

        // Transform to array of { name, count }
        let result = [];
        for (let [name, count] of map) {
            result.push({value: name, count: count});
        }

        return result;
    }

    render() {
        return (
            <tr>
                <th className="name" scope="header">{this.props.searchResult.name}</th>
                <td className="review"><GooglePlacesReview urlOfReview={this.state.googleMapsUrl} reviewScore={this.state.reviewScore}/></td>
                <td className="wordCloud"><TagCloud 
                        minSize={10}
                        maxSize={20}
                        tags={this.getData()}/></td>
                <td> 
                    <a href={this.state.mailToText} target="_blank" data-original-title="Tooltip on top">
                        <i className="fa fa-envelope"></i>
                    </a>
                </td>
            </tr>
        );
    }
}