"""
Routes and views for the flask application.
"""
import uuid
import math

# import config
# import pydocumentdb.document_client as document_client

from datetime import datetime
from flask import render_template, jsonify
from FlaskWebProject import app

locations = [(37.8707,-122.2515, 'http://live.calhacks.io'), (48.8566, 2.3522, 'https://en.wikipedia.org/wiki/Paris'), (37.8721, -122.2578,
    'http://campanile.berkeley.edu'), (37.86798, -122.266212, 'https://www.yelp.com/biz/u-cha-berkeley'), (37.870102, -122.268148, "https://www.bart.gov/schedules/bystation"), (37.8756, -122.2588, 'https://people.eecs.berkeley.edu/~sequin/soda/soda.html'),
(37.8741, -122.2639, 'http://access-guide.berkeley.edu/buildings/tolman-hall'), (37.8716, -122.2538, 'http://haas.berkeley.edu')]

@app.route('/')
def home():
    return "Endpoint hit!!!!"



@app.route('/about')
def about():
    """Renders the about page."""
    return render_template(
        'about.html',
        title='About',
        year=datetime.now().year,
        message='Your application description page.'
    )


# def distance(x1,y1, x2,y2):
#     return 1#((x1-x2))#**2) #+ (y1-y2)**2)**1/2

def translate(degree):
    return degree * 3.1415926 * 6.371 * (10 ** 6) /180


@app.route('/<lat>/<lon>', methods=['GET', 'POST'])
def locate(lat, lon):
    x1 = float(lat)
    lat = x1
    y1 = float(lon)
    lon = y1

    distances = [None] * len(locations)

    closest = 0

    for i, l in enumerate(locations):
        x2 = float(l[0])
        y2 = float(l[1])

        distances[i] = ((x1-x2)**2 + (y1-y2)**2)**1/2

        if distances[closest] > distances[i]:
            closest = i


    if (250 > translate(distances[closest])):
        return locations[closest][2]
    else:
        return jsonify(lat=lat,lon=lon)
