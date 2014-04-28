================================================================================================
		   	  Proiect PA - Şah 2013 - 2014

Nume echipă: The Minions
An: II
Materie: Proiectarea Algoritmilor
================================================================================================

Etapa I

	Majoritatea metodelor implementate în cadrul acestei etape sunt
scurte şi comentate suficient în cod. Vom încerca în Readme să explicăm
metodele şi variabilele care pot reprezenta o sursă de ambiguitate:

Metoda "moveEngine()" mută un pion, explicaţii:
	Variabilele:
		takePieceLeft - este egală cu 1 dacă există piesă a adversarului
				pe care ar putea-o lua în partea stângă.
		takePieceRight - este egală cu 1 dacă există piesă a adversarului
				 pe care ar putea-o lua în partea dreaptă.
		goAhead - este egală cu 1 dacă nu are piesă în faţa sa;
		colorState - variabilă membră a clasei este egală cu -1 dacă engine-ul
			     joacă cu piesele negre (având în vedere că piesele negre
			     sunt reprezentate cu valori negative) şi este egală cu 1
		             dacă engine-ul joacă cu piesele albe (având în vedere că 
		  	     piesele albe sunt reprezentate cu valori pozitive).
	Detalii implementare:
		Cazurile în care engine-ul nu mai are posibilitatea unei mutări valide:
		atunci când atât takePieceLeft, takePieceRight şi goAhead sunt 0 (are
		piesa în faţa sa şi nu poate lua nicio altă piesă în diagonală), atunci
		când a ajuns fie pe prima linie, fie pe ultima în funcţie de culoarea
		engienul-ui (chiar dacă îşi schimbă piesa, mutările verificate sunt pentru
		pion), atunci când i-a fost luat pionul cu care muta de către adversar (ceea
		ce se traduce la nivelul codului prin verificarea că pe poziţia curentă este
		o piesă de alt semn decât era înainte) în toate aceste cazuri metoda returnează
		"false".
		Ceea ce ramâne de stabilit în cazul în care engine-ul mai are mutări
		este să ia o decizie în ceea ce priveşte mutarea următoare. Cazurile în care
		are o singură variantă sunt clare, iar atunci cand are mai multe optiuni,
		folosind Random rand = new Random(); int choice = rand.nextInt(numărOpţiuni);
		ia o decizie la "întâmplare" în ce direcţie să o ia.
		După o mutare valorile variabilelor lineTest şi columnTest se modifică astfel:
		lineTest - dacă engine-ul joacă cu negru atunci linia scade (lineTest = lineTest + colorState
		şi colorState = -1 în acest caz), dacă engine-ul joacă cu alb atunci linia creşte
		(lineTest = lineTest + colorState şi colorState = 1 în acest caz).
		columnTest - aceasta creşte sau descreşte dacă merge în diagonală şi ia o piesă adversă
		sau rămâne nemodificată în cazul în care merge în faţă.	

	Comenzile "white" şi "black" sunt ambiguu prezentate în documentaţie http://www.gnu.org/software/xboard/engine-intf.html.
Deschizând xboard cu comanda xboard -debug şi jucând cu engine-ul fairymax şi dând comanda Machine White am observat următoarele:
vizual engine-ul trece pe alb şi este rândul engine-ului nicidecum "Set White on move. Set the engine to play Black", iar
comanda Machine Black trece engine-ul pe negru şi este rândul engine-ului nicidecum "Set Black on move. Set the engine to play White".
	Mai departe am deschis fişierul de debug şi am remarcat că xboard-ul trimitea următoarele comenzi la acţionarea Machine White: "black"
urmată imediat de comanda "white".
	În urma acestor observaţii am implementat cele două comenzi.
	
======================================================================================================================

Etapa II
		
		**************************************************************************************************************************************** 
		* (-colorState) table[line][column] < 0 - piesa este proprie engine-ului, explicaţie: 
		* engine - negru => colorState = -1 => (-(-1)) * Valoare_Negativa = 1 * Valoare_Negativa = Valoare_Negativa < 0 => piesă proprie
		* engine - alb => colorState = 1 => (-1) * Valoare_Pozitiva = Valoare_Negativa < 0 => piesă proprie
		* Dacă rezultatul este > 0 atunci este o piesă a adversarului.
		****************************************************************************************************************************************
		Am ales o astfel de implementare pentru a nu scrie cod inutil care consta într-o singură modificare de inegalitate, astfel am înjumătăţit
		dimensiunea codului.
		****************************************************************************************************************************************
		Bazându-ne în continuare pe faptul că schimbarea culorii enginelui se realizează pe baza acţionării comenzii Machine White/Black
modificându-se astfel valoarea variabilei colorState corespunzător culorii enginelui, respectiv -1 pentru engine culoare neagră şi 1
pentru engine culoare albă.
		Metoda "check" verifică toate cazurile prin care regele s-ar putea afla în poziţie de şah:
			check_up, check_down, check_left, check_right verifică poziţie cu poziţie sus, jos, la stânga şi respectiv dreapta oprindu-se
fie în momentul în care întâlneşte în calea sa o piesă proprie, fie în momentul în care întâlneşte o piesă a adversarului, sau se
opreşte când se "loveşte" de marginile tablei de joc. În cazul în care mai întâi pe aceea direcţie s-a aflat o piesă a adversarului
atunci se verifică dacă este tură sau regină.
			check_upLeft, check_upRight, check_downRight, check_downLeft verifică cele patru diagonale, gândirea fiind aceeaşi ca şi în cazul
metodelor explicate anterior, diferenţele constând în: se verifică dacă piesa adversarului este regină sau nebun, singurele piese care
acţionează pe diagonală, de asemenea în aceste cazuri şi pionul poate da şah, dar am ţinut cont de faptul că un pion trecut de poziţia
regelui cu o linie nu îl pune în această situaţie, deoarece pionul nu se poate întoarce.
			check_Horse verifică toate cele opt posibile poziţii ale unui cal care ar pune regele în şah.
			check_King verifică dacă regele advers s-ar afla în imediata vecinătate a regelui engine-ului, mutarea fiind ilegală.

=======================================================================================================================

Etapa III

	În aceasta etapa am implementat un algoritm Negamax bazat pe greutăți (valori diferite) acordate pieselor și pe
bonusuri de poziție (puncte/valori suplimentare acordate în funcție de poziție). Astfel, la începutul fișierului
ChessMain.java, in clasa ChessMain, am predefinit câte o matrice cu bonusuri de poziție pentru fiecare piesă.
	
	Greutăți inițiale atribuite fiecărei piese:
	      
	      PIESĂ     greutate (puncte)  |   TABELĂ bonusuri de poziție asociată piesei
	    ------------------------------------------------------------------------------
	    * PION   ->   150 puncte	   |   PawnTableWhite		PawnTableBlack
	    * CAL    ->   320 puncte	   |   KnightTableWhite		KnightTableBlack
	    * NEBUN  ->   330 puncte	   |   BishopTableWhite		BishopTableBlack
	    * TURN   ->   500 puncte	   |   RookTableWhite		RookTableBlack
	    * REGINA ->   900 puncte	   |   QueenTableWhite		QueenTableBlack
	    * REGE   -> 20000 puncte	   |   KingTableMiddleWhite	KingTableMiddleBlack

	    
	Intrând puțin în detalii de implementare, noutatea adusă engenului în această etapă o reprezintă funcția
negaMax, care se folosește de o funcție auxiliară eval. În ciuda faptului că funcția negaMax este funcția principală în 
implementarea algoritmului Negamax, atracția centrală este reprezentată de funcția auxiliară eval, care calculează
ponderea pieselor la fiecare mutare adunând bonusurile aferente în funcție de poziția actuală și poziția viitoare.


OBS.: Adâncimea maximă a arborelui creat prin recursivitate este ținută în variabila MAXDEPTH care are valoarea 3.
Întrucât adâncimea este una relativ mică, motorul nu reușește să vadă în avans decât două mutări proprii și o
mutare a adversarului. Am ales această valoare deoarece orice altă valoare a adâncimii mai mare de 3 duce la o creștere
exponențială a timpului de calculare a mutărilor, astfel durata unui joc de șah depășind cu mult timpul acordat
unei partide.


CONCLUZII: Pentru etapa următoare avem în vedere creșterea adâncimii maxime a arborelui în jurul valorii de 5-6 prin
folosirea unui algoritm Alpha-Beta Pruning, pentru reducerea numarului de ramuri ale arborelui. Astfel, ne dorim să
reușim să scădem timpul necesar unei mutări (pe MAXDEPTH = 5) de la 3 min/mutare la 5-10 sec/mutare.

========================================================================================================
	MOD DE RULARE ȘI FIȘIERE ADIȚIONALE:

	Fișiere adiționale:
		- Makefile - pe lângă fișierul sursă cu extensia .java, am adăugat și un fișier Makefile care are
			     implementate funcțiile: build, clean și default.
			   - default: executarea comenzii "make" fără argumente, duce la executarea implicită a
			   	       comenzii "build"
			   - build: executarea comenzii "make" cu argumentul "build", are rolul de a compila sursa
			  	     .java (javac ChessMain.java), generând fișierele executabile .class, și oferindu-le
				     permisiuni de execuție (chmod 777 *.class) pentru a putea fii încărcate de xboard
			   - clean: executarea comenzii "make" cu argumetul "clean", are rolul de a șterge toate
			   	    fișierele executabile (*.class), generate în urma compilării sursei.


