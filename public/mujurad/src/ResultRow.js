import React from 'react';
import axios from 'axios';
import GooglePlacesReview from './GooglePlacesReview'
import { TagCloud } from "react-tagcloud";

const PLACES_API_KEY = 'AIzaSyBzLHOdWHy_0eY4j3SYYbEIKU5OtmGOo2U'

export default class ResultRow extends React.Component {

    constructor(props) {
        super(props);
        this.state = { 
            reviewScore: null            
        }

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

    render() {
        return (
            <tr>
                <td>{this.props.searchResult.name}</td>
                <td><GooglePlacesReview urlOfReview={this.state.googleMapsUrl} reviewScore={this.state.reviewScore}/></td>
                <td>[word cloud]</td>
                <td>[hejtmail]</td>
            </tr>
        );
    }
}