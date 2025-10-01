import { Routes } from '@angular/router';
import { Home } from './components/home/home';
import { ConnectRepo } from './components/connect-repo/connect-repo';
import { Stories } from './components/srories/srories';

export const routes: Routes = [
    {path: "", redirectTo: "Home",pathMatch:"full"},
    {path: "Home", component: Home, title: "home"},
    {path: "Connect_Repo", component: ConnectRepo, title: "Connect Repo"},
    {path: "Stories", component: Stories, title: "Stories"},
];
