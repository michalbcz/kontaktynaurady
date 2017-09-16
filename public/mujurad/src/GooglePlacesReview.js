import React from 'react';
import ReactStars from 'react-stars';

export default class GooglePlacesReview extends React.Component {

    render() {

         if (this.props.reviewScore != null) {
            // if (this.props.reviewScore === 0) {
            //     return (
            //         <span></span>   
            //     )
            // }

            return (
                <a href={this.props.urlOfReview} target="_blank">
                 <span style={{ display: "none"}}>{ this.props.reviewScore } / 5</span>
                 <ReactStars
                        count={5}
                        value={this.props.reviewScore}
                        edit={false}                   
                        size={24} 
                        color2={'#ffd700'} />
                </a>
            )
         } else {
           return <span className="fa fa-spinner fa-spin" style={{ "font-size" : "24px" }} />             
         }

    }

}