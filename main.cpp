#include <iostream>
#include <fstream>
#include <cstdlib>
#include <vector>
#include <sstream>
#include <cstring>
#include <time.h>
#include <unordered_map>
#include <queue>
#include <algorithm>
#include <climits>
#include <csignal>
#include <windows.h>
//nym's lab1
#define Max_Num 0x3ff
#define Max_iter 1000
volatile bool stopTraversal = false;
struct node{
	std::string name;
	unsigned seq;
	struct node* pHashList; //连接散列表
	std::vector<std::pair<struct node*,int>> outNodeList;
	std::vector<struct node*> inNodeList;
	double rankValue;
	unsigned inDegree;
	unsigned outDegree;
};
class Graph{
public:
	struct node nodeList[Max_Num+1];
	bool flag[Max_Num+1];
	unsigned nodeNum;
	unsigned edgeNum;
	std::vector<struct node*> nodeSet;
	Graph(void):nodeNum(0),edgeNum(0){
		memset(nodeList,0,sizeof(nodeList));
		memset(flag,0,sizeof(flag));
		nodeSet.clear();
	}
	bool createGraphFromTxt(char* filePath);
	bool showGraphInDot(std::string filepath);
	void showGraph(std::string src, std::string dst);
	std::vector<std::string> searchBridgeWord(std::string from,std::string to);
	static void wordsFliter(std::string& name);
	struct node* findNode(std::string name);
	~Graph();
private:
	static int hash(std::string name){
		unsigned index=0,i;
		for (char c : name){
			index = (index<<2)+c;
			if(i=(index&~Max_Num)) index = ((i>>8)^index)&Max_Num;
		}
		return index;
	}
	bool insertEdge(std::string firName ,std::string SecName);
	struct node* insertNode(std::string name);
};
class Function{
public:
	static void showBridgeResult(Graph& graph);
	static void geneNewWords(Graph& graph);
	static void shortPath(Graph& graph);
	static void cacuPageRank(Graph& graph);
	static void randomMove(Graph& graph,std::string filepath);
private:
	static void printAllPaths(Graph& graph, struct node* start, struct node* target, std::unordered_map<struct node*, std::vector<struct node*>> predecessors);
	
};
void printMenu(){
	std::ifstream inFile;
	inFile.open("./menu.txt");
	if(!inFile){
		std::cerr<<"Menu open error"<<std::endl;
	}
	std::string line;
	while(std::getline(inFile,line)){
		std::cout<<line<<std::endl;
	}
	std::cout<<"Your choice is:";
	inFile.close();
}
void handleInterrupt(int signal) {
    if (signal == SIGINT) { // 捕获 Ctrl+C 信号
        stopTraversal = true;
        std::cout << "\nTraversal interrupted by user (Ctrl+C)." << std::endl;
    }
}
int main(int argc, char* argv[]){
	Graph graph;
	if(argc<2){
		std::cout<<"Help:\n .exe pathTofile/filename"<<std::endl;
		system("pause");
		return 0;
	}
	if(!graph.createGraphFromTxt(argv[1])){
		std::cout<<"Exit"<<std::endl;
		system("pause");
		return -1;
	}
	Function::cacuPageRank(graph);
	srand(time(NULL));
	char choice=0;
	while(1){
		system("cls");
		printMenu();
		std::cin>>choice;
		choice = choice-'0';
		while(getchar()!='\n') getchar();
		if(choice==0){
			exit(0);
		}
		else if(choice==1){ 
			std::string dotfile;
			std::cout<<"Please into the dot file path:";
			std::getline(std::cin,dotfile);
			if(graph.showGraphInDot(dotfile)){
				std::cout<<"Success"<<std::endl;
			}
			else std::cout<<"Error"<<std::endl;
		}
		else if(choice==2){
			std::string dotfile;
			std::cout<<"Please into the dot file path:";
			std::getline(std::cin,dotfile);
			std::string pngfile;
			std::cout<<"Please into the png file path:";
			std::getline(std::cin,pngfile);
			graph.showGraph(dotfile,pngfile);
			std::cout<<"Success"<<std::endl;
		}
		else if(choice==3){
			Function::showBridgeResult(graph);
		}
		else if(choice==4){
			Function::geneNewWords(graph);
		}
		else if(choice==5){
			Function::shortPath(graph);
		}
		else if(choice==6){
			std::string node;
			std::cout<<"Please input the node you wanto search(-all mean all):";
			std::cin>>node;
			if(node=="-all"){
				for(int i=0;i<graph.nodeSet.size();i++){
					struct node* pnode = graph.nodeSet[i];
					std::cout<<"the PK of node \""<<pnode->name<<"\" is "<<pnode->rankValue<<std::endl;
				}
			}
			else{
				struct node* pnode = graph.findNode(node);
				if(!pnode){
					std::cout<<"node \""<<node<<"\" don't exist"<<std::endl;
				}
				else{
					std::cout<<"the PK of node \""<<node<<"\" is "<<pnode->rankValue<<std::endl;
				}
			}
		}
		else if(choice==7){
			std::string outfile;
			std::cout<<"Please into the output file path:";
			std::getline(std::cin,outfile);
			Function::randomMove(graph,outfile);
		}
		else if(choice==8){
			std::cout<<graph.nodeNum<<'\t'<<graph.edgeNum<<std::endl;
			for(int i=0;i<graph.nodeSet.size();i++){
				std::cout<<graph.nodeSet[i]->name<<" inDegree:"<<graph.nodeSet[i]->inDegree<<" outDegree:"<<graph.nodeSet[i]->outDegree<<std::endl;
			}
		}
		else{
			std::cerr<<"Wrong choice"<<std::endl;
		}
		system("pause");
	}
	return 0;
}
bool Graph::insertEdge(std::string firName ,std::string SecName){
	int index1 = hash(firName);
	int index2 = hash(SecName);
	struct node* pFir,*pSec;
	if(!(pFir=findNode(firName))){
		pFir = insertNode(firName);
	}
	if(!(pSec=findNode(SecName))){
		pSec = insertNode(SecName);
	}
	for(std::vector<std::pair<struct node*,int>>::iterator it=pFir->outNodeList.begin(); \
			it!=pFir->outNodeList.end();it++){
		if(it->first->name == SecName){
			(it->second)++;
			return true;
		}
	}
	pFir->outNodeList.emplace_back(std::pair<struct node*,int>(pSec,1));
	pFir->outDegree++;
	pSec->inNodeList.emplace_back(pFir);
	pSec->inDegree++;
	this->edgeNum++;
	return true;
}
struct node* Graph::findNode(std::string name){
	int index = hash(name);
	if(!flag[index]) return NULL;
	struct node* pn = nodeList+index;
	while(pn){
		if(pn->name == name) return pn;
		pn = pn->pHashList;
	}
	return pn;
}
struct node* Graph::insertNode(std::string name){
	int index = hash(name);
	if(!flag[index]){
		nodeList[index].name = name;
		nodeList[index].seq =(this->nodeNum)++; 
		nodeList[index].inDegree=0;
		nodeList[index].outDegree=0;
		flag[index]=true;
		this->nodeSet.emplace_back(&nodeList[index]);
		return &nodeList[index];
	}
	struct node* pn = &(nodeList[index]);
	while(pn->pHashList){
		if(pn->name == name){
			std::cerr<<"The node have existed"<<std::endl;
			return pn;
		}
		pn = pn->pHashList;
	}
	if(pn->name == name){
		std::cerr<<"The node have existed"<<std::endl;
		return pn;
	}
	struct node* pnew = new node{name,((this->nodeNum)++),nullptr,std::vector<std::pair<struct node*,int>>(),std::vector<struct node*>(),0,0};
	pn->pHashList = pnew;
	this->nodeSet.emplace_back(pnew);
	return pnew;
}
bool Graph::createGraphFromTxt(char* filePath){
	std::ifstream infile;
	infile.open(filePath,std::ios::in);
	if(!infile){
		std::cerr<<"Unable to open file "<<filePath<<std::endl;
		return false;
	}
	std::string word1;
	std::string word2;
	while(infile>>word2){
		wordsFliter(word2);
		if(!word2.empty()){
			if(word1.empty());
			else{
				if(!this->insertEdge(word1,word2)){
					std::cerr<<"Edge insert error"<<std::endl;
				}
			}
			word1 = word2;
		}
	}
	infile.close();
	return true;
}
bool Graph::showGraphInDot(std::string filepath){
	std::ofstream outfile;
	outfile.open(filepath,std::ios::out);
	if(!outfile){
		std::cerr<<filepath<<" don't open"<<std::endl;
		return false;
	}
	outfile<<"digraph G {\n";
	for(int i=0;i<nodeSet.size();i++){
		struct node* pt =nodeSet[i];
		for(int j=0;j<pt->outNodeList.size();j++){
			outfile<<"\t"<<"\""<<pt->name<<"\""<<" -> "
			<<"\""<<pt->outNodeList[j].first->name <<"\""
			<<" [label="<<pt->outNodeList[j].second<<"];\n";
		}
	}
	outfile<<"}";
	outfile.close();
	return true;

}
Graph::~Graph(){
	for(int i=0;i<=Max_Num;i++){
		if(!flag[i]) continue;
		struct node* pt = &nodeList[i];
		pt->inNodeList.clear();
		pt->outNodeList.clear();
		pt = pt->pHashList;
		while(pt){
			struct node* ptmp = pt;
			pt = pt->pHashList;
			pt->inNodeList.clear();
			pt->outNodeList.clear();
			delete(ptmp);
		}
	}
}
void Graph::showGraph(std::string src, std::string dst){
	std::string command = std::string(".\\bin\\dot.exe"
	" -Tpng ")+src+std::string(" -o ")+dst;
	system(command.c_str());
}
std::vector<std::string> Graph::searchBridgeWord(std::string from,std::string to){
	wordsFliter(from);
	wordsFliter(to);
	struct node* pfrom = findNode(from);
	struct node* pto = findNode(to);
	std::vector<std::string> bridge;
	if(!pfrom);
	else if(!pto);
	else{
		for(int i=0;i<pfrom->outNodeList.size();i++){
			for(int j=0;j<pto->inNodeList.size();j++){
				if(pfrom->outNodeList[i].first==pto->inNodeList[j]){
					bridge.emplace_back(pto->inNodeList[j]->name);
				}
			}
		}

	}
	return bridge;
}
void Graph::wordsFliter(std::string& name){
	for(int i=0;i<name.length();){
		if('a'<=name[i] and name[i]<='z'){
			i++;
		}
		else if(name[i]>='A' and name[i]<='Z'){
			name[i] -= ('A'-'a');
			i++;
		}
		else name.erase(i,1);
	}
}
void Function::geneNewWords(Graph& graph){
	char userInput[1024]={0};
	std::cout<<"Please input a line of words(less than 1023 chars)"<<std::endl;
	std::cin.getline(userInput,1023);
	std::string word1;
	std::string word2;
	std::istringstream stream(userInput);
	std::vector<std::string> bridge;
	while(stream>>word2){
		Graph::wordsFliter(word2);
		if(word2.empty()) continue;
		else{
			if(word1.empty());
			else{
				bridge = graph.searchBridgeWord(word1,word2);
				std::cout<<word1<<" ";
				if(!bridge.empty()){
					std::cout<<bridge[rand()%(bridge.size())]<<" ";
				}
			}
			word1 = word2;
		}
	}
	std::cout<<word1<<" "<<std::endl;
}
void Function::showBridgeResult(Graph& graph){
	std::string word1;
	std::string word2;
	std::cout<<"Please input the word1 word2"<<std::endl;
	std::cin>>word1>>word2;
	Graph::wordsFliter(word1);
	Graph::wordsFliter(word2);
	struct node* pfrom = graph.findNode(word1);
	struct node* pto = graph.findNode(word2);
	if(!pfrom and pto) std::cerr<<"No \""<<word1<<"\" in the graph!"<<std::endl;
	else if(!pto and pfrom) std::cerr<<"No \""<<word2<<"\" in the graph!"<<std::endl;
	else if(!pto and !pfrom) std::cerr<<"No \""<<word1<<"\" and \""<<word2<<"\" in the graph!"<<std::endl;
	else{
		std::vector<std::string> bridge = graph.searchBridgeWord(word1,word2);
		if(bridge.empty()){
			std::cout<<"No bridge words from \""<<word1<<"\" to \""<<word2<<"\""<<std::endl;
		}
		else{
			std::cout<<"The bridge words from \""<<word1<<"\" to \""<<word2<<"\" are: "<<"\""<<bridge[0]<<"\"";
			for(int i=1;i<bridge.size();i++){
				std::cout<<","<<"\""<<bridge[i]<<"\"";
			}
			std::cout<<std::endl;
		}
	}	
}
void Function::shortPath(Graph& graph) {
    std::string startNode, endNode,word;
	char line[1024]={0};
    std::cout << "Please input the start node (and optionally the end node):" << std::endl;
	std::cin.getline(line,1023);
	std::stringstream stream(line);
	for(int i=0;i<=1 and stream>>word;i++){
		if(i==0) startNode=word;
		else endNode=word;
	}	

    // 过滤输入的节点名称
    Graph::wordsFliter(startNode);
    struct node* start = graph.findNode(startNode);
    if (!start) {
        std::cerr << "No \"" << startNode << "\" in the graph!" << std::endl;
        return;
    }

    // Dijkstra's algorithm
    std::unordered_map<struct node*, int> distances; // 存储最短距离
    std::unordered_map<struct node*, std::vector<struct node*>> predecessors; // 存储前驱节点列表
    struct Compare {
        bool operator()(const std::pair<struct node*, int>& a, const std::pair<struct node*, int>& b) const {
            return a.second > b.second; // 按距离升序排列
        }
    };
    std::priority_queue<std::pair<struct node*, int>, std::vector<std::pair<struct node*, int>>, Compare> pq;

    // 初始化距离
    for (std::vector<struct node*>::iterator it = graph.nodeSet.begin(); it != graph.nodeSet.end(); ++it) {
        distances[*it] = INT_MAX;
    }
    distances[start] = 0;
    pq.push(std::make_pair(start, 0));

    while (!pq.empty()) {
        std::pair<struct node*, int> top = pq.top();
        struct node* current = top.first;
        int currentDist = top.second;
        pq.pop();

        // 如果当前距离已经大于记录的最短距离，跳过
        if (currentDist > distances[current]) continue;

        // 遍历当前节点的所有邻接节点
        for (std::vector<std::pair<struct node*, int>>::iterator edge = current->outNodeList.begin();
             edge != current->outNodeList.end(); ++edge) {
            struct node* neighbor = edge->first;
            int weight = edge->second;

            // 松弛操作
            if (distances[current] + weight < distances[neighbor]) {
                distances[neighbor] = distances[current] + weight;
                predecessors[neighbor].clear(); // 清空旧的前驱节点
                predecessors[neighbor].push_back(current); // 添加新的前驱节点
                pq.push(std::make_pair(neighbor, distances[neighbor]));
            } else if (distances[current] + weight == distances[neighbor]) {
                // 如果找到另一条等长路径，添加前驱节点
                predecessors[neighbor].push_back(current);
            }
        }
    }

    // 回溯并打印所有路径
    if (endNode.empty()) {
        std::cout << "Shortest paths from \"" << startNode << "\":" << std::endl;
        for (std::vector<struct node*>::iterator it = graph.nodeSet.begin(); it != graph.nodeSet.end(); ++it) {
            struct node* target = *it;
            if (target == start) continue;

            if (distances[target] == INT_MAX) {
                std::cout << "\nNo path to \"" << target->name << "\"." << std::endl;
            } else {
                std::cout << "\nTo \"" << target->name << "\": Distance = " << distances[target] << ", Paths:" << std::endl;
                printAllPaths(graph, start, target, predecessors);
            }
        }
    } else {
        struct node* end = graph.findNode(endNode);
        if (!end) {
            std::cerr << "No \"" << endNode << "\" in the graph!" << std::endl;
            return;
        }

        if (distances[end] == INT_MAX) {
            std::cout << "No path from \"" << startNode << "\" to \"" << endNode << "\"." << std::endl;
        } else {
            std::cout << "Shortest paths from \"" << startNode << "\" to \"" << endNode << "\": Distance = " << distances[end] << ", Paths:" << std::endl;
            printAllPaths(graph, start, end, predecessors);
        }
    }
}
void Function::printAllPaths(Graph& graph, struct node* start, struct node* target, std::unordered_map<struct node*, std::vector<struct node*>> predecessors) {
    std::vector<std::string> currentPath;
    currentPath.push_back(target->name);
    while (!currentPath.empty()) {
        std::string currentNode = currentPath.back();

        // 如果到达起点，保存路径
        if (currentNode == start->name) {
            std::vector<std::string> path(currentPath.rbegin(), currentPath.rend());
			std::cout<<path[0];
			for (int i = 1; i < path.size(); ++i) {
				std::cout << " -> "<< path[i];
			}
			std::cout << std::endl;
            currentPath.pop_back();
            continue;
        }

        // 如果还有前驱节点，继续回溯
        struct node* current = graph.findNode(currentNode);
        if (!predecessors[current].empty()) {
            struct node* next = predecessors[current].back();
            predecessors[current].pop_back();
            currentPath.push_back(next->name);
        } else {
            // 如果没有前驱节点，回溯
            currentPath.pop_back();
        }
    }
}
void Function::cacuPageRank(Graph& graph){
	double d=0.85;
	for(int i=0;i<graph.nodeSet.size();i++){
		struct node* pnode = graph.nodeSet[i];
		pnode->rankValue = 1.0/graph.nodeNum;
	}
	double max_dif=1;
	int iter=0;
	while(max_dif>=1e-7 and iter<=Max_iter){
		double deadNodePRSum=0;
		max_dif=0;
		std::vector<double> oldPR(graph.nodeSet.size(),0);
		for(int i=0;i<graph.nodeSet.size();i++){
			struct node* pnow = graph.nodeSet[i];
			oldPR[i]=pnow->rankValue;
			if(pnow->outDegree==0){
				deadNodePRSum+=pnow->rankValue;
			}
		}
		double fix = deadNodePRSum/graph.nodeNum;
		for(int i=0;i<graph.nodeSet.size();i++){
			double newPr=0;
			struct node* pnow = graph.nodeSet[i];
			for(std::vector<struct node*>::iterator it = pnow->inNodeList.begin();
				it!=pnow->inNodeList.end();it++){
					newPr+=(oldPR[(*it)->seq])/((*it)->outDegree);
				}
				newPr+=fix;
				pnow->rankValue = d*newPr+(1-d)*1.0/graph.nodeNum;
		}
		
		for(int i=0;i<graph.nodeSet.size();i++){
			struct node* pnow = graph.nodeSet[i];
			max_dif = std::max(max_dif,std::abs(oldPR[i]-pnow->rankValue));
		}
		iter++;
	}
}
void Function::randomMove(Graph& graph,std::string filepath){
	stopTraversal = false;
	std::signal(SIGINT, handleInterrupt);
	struct node* pfirst = graph.nodeSet[rand()%graph.nodeNum];
	std::ofstream output;
	output.open(filepath,std::ios::out);
	if(!output.is_open()){
		std::cerr<<"File "<<filepath<<" not open"<<std::endl;
		return;
	}
	int nowlen = 0;
	int maxlen = 2048;
	char buffer[maxlen]={0};
	bool is_end=false;
	while(1){
		if(nowlen+pfirst->name.size()+1<=maxlen){
			snprintf(buffer+nowlen,pfirst->name.size()+2,"%s ",pfirst->name.c_str());
			std::cout<<pfirst->name<<" ";
			nowlen+=(pfirst->name.size()+1);
		}
		else{
			output<<buffer;
			memset(buffer,0,sizeof(buffer));
			nowlen = 0;
			continue;
		}
		if(pfirst->outDegree==0){
			std::cout<<std::endl<<"meet a node outDegree = 0"<<std::endl;
			is_end = true;
		}
		else{
			int randomIndex = rand()%pfirst->outNodeList.size();
			if(pfirst->outNodeList[randomIndex].second>0){
				pfirst->outNodeList[randomIndex].second = pfirst->outNodeList[randomIndex].second*(-1);
				pfirst=pfirst->outNodeList[randomIndex].first;
			}
			else{
				std::cout<<std::endl<<"meet the same edge again"<<std::endl;
				is_end=true;
			}
		}
		Sleep(200);
		if(is_end or stopTraversal){
			std::cout<<"Preparing for Exit"<<std::endl;
			if(nowlen>0){
				output<<buffer;
			}
			output.close();
			std::signal(SIGINT, SIG_DFL);
			for(int i=0;i<graph.nodeSet.size();i++){
				for(int j=0;j<graph.nodeSet[i]->outNodeList.size();j++){
					if(graph.nodeSet[i]->outNodeList[j].second<0){
						graph.nodeSet[i]->outNodeList[j].second = graph.nodeSet[i]->outNodeList[j].second*(-1);
					}
				}
			}
			return;
		}

	}
}
