==================================================================================
		   	  Proiect PA - Şah 2013 - 2014

Nume echipă: The Minions
An: II
Materie: Proiectarea Algoritmilor
==================================================================================

Etapa I:
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
	
==================================================================================
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
