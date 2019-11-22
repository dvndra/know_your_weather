import { Injectable } from '@angular/core';
import { Http } from '@angular/http';

@Injectable({
  providedIn: 'root'
})
export class CurrentService {

  // latitude=42.3601&longitude=-71.0589
  private url = "http://devendra.us-east-2.elasticbeanstalk.com/api/current/?";
  constructor(private http : Http) { }

  getData(query: string){
    return this.http.get(this.url+encodeURI(query));
  }
}